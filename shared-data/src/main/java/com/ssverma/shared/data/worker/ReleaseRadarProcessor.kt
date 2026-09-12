package com.ssverma.shared.data.worker

import android.content.Context
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.RemoteProviderInfo
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.notifications.ShowTimeNotificationManager
import com.ssverma.shared.data.R
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.domain.model.release.ReleaseRadarConfig
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.formatAsIso
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result state returned by [ReleaseRadarProcessor].
 */
enum class ReleaseRadarResult {
    Success,
    Retry
}

/**
 * Robust execution engine for the Smart Release Radar background task.
 * Evaluates theatrical releases (0 network calls) and subscription streaming arrivals (strictly rate-limited).
 */
@Singleton
class ReleaseRadarProcessor @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val tmdbApiService: TmdbApiService,
    private val appConfigRepository: AppConfigRepository,
    private val appConfigProvider: AppConfigProvider,
    private val notificationManager: ShowTimeNotificationManager,
    @param:ApplicationContext private val context: Context
) {
    /**
     * Executes the daily radar check for watchlist titles.
     *
     * @param currentDate Current date to evaluate against release dates. Defaults to system date.
     * @return [ReleaseRadarResult.Success] on clean completion or intentional no-op, [ReleaseRadarResult.Retry] on fatal exception.
     */
    suspend fun executeRadar(currentDate: LocalDate = DateUtils.currentDate()): ReleaseRadarResult {
        return try {
            // 1. Remote Kill-Switch Check
            val isRemotelyEnabled = appConfigProvider.getBoolean(
                ReleaseRadarConfig.REMOTE_KEY_RELEASE_RADAR_ENABLED,
                ReleaseRadarConfig.DEFAULT_RELEASE_RADAR_ENABLED
            )
            if (!isRemotelyEnabled) {
                return ReleaseRadarResult.Success
            }

            // 2. Local User Preference & Notification Settings
            val isNotificationsEnabled = appConfigRepository.isNotificationsEnabled.first()
            val isReleaseRadarEnabled = appConfigRepository.isReleaseRadarEnabled.first()
            if (!isNotificationsEnabled || !isReleaseRadarEnabled) {
                return ReleaseRadarResult.Success
            }

            // 3. Android System Notification Permission
            if (!notificationManager.hasNotificationPermission()) {
                return ReleaseRadarResult.Success
            }

            val todayIso = currentDate.formatAsIso().orEmpty()
            if (todayIso.isBlank()) {
                return ReleaseRadarResult.Success
            }

            var notificationDispatched = false

            // Tier 1: Theatrical Premiere Radar (0 TMDB API calls - pure local Room query)
            val theatricalCandidates = watchlistDao.getTheatricalReleaseAlertCandidates(todayIso)
            for (candidate in theatricalCandidates) {
                if (candidate.title.isNotBlank() && !notificationDispatched) {
                    val title = context.getString(
                        R.string.release_radar_theatrical_title,
                        candidate.title
                    )
                    val message = context.getString(
                        R.string.release_radar_theatrical_desc
                    )
                    val imageUrl = candidate.posterImageUrl.ifBlank { candidate.backdropImageUrl }
                    val deepLink = buildMovieDeepLink(candidate.mediaId)

                    notificationManager.showReminderNotification(
                        title = title,
                        message = message,
                        imageUrl = imageUrl.ifBlank { null },
                        deepLink = deepLink
                    )
                    notificationDispatched = true
                }
                // Always mark as notified to prevent re-alerts
                watchlistDao.markTheatricalNotified(candidate.mediaId)
            }

            // Tier 2: Streaming Arrival Radar (Strictly Capped: Max 3 TMDB API calls per day)
            // If already notified for theatrical premiere today, skip streaming checks to avoid notification spam
            if (!notificationDispatched) {
                val minReleaseDateIso =
                    currentDate.minusDays(ReleaseRadarConfig.STREAMING_WINDOW_MAX_DAYS)
                        .formatAsIso().orEmpty()
                val maxReleaseDateIso =
                    currentDate.minusDays(ReleaseRadarConfig.STREAMING_WINDOW_MIN_DAYS)
                        .formatAsIso().orEmpty()
                val minRecheckEpochMs =
                    System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ReleaseRadarConfig.STREAMING_RECHECK_COOLDOWN_DAYS)

                val streamingCandidates = watchlistDao.getStreamingRadarCandidates(
                    minReleaseDateIso = minReleaseDateIso,
                    maxReleaseDateIso = maxReleaseDateIso,
                    minRecheckEpochMs = minRecheckEpochMs,
                    limit = ReleaseRadarConfig.MAX_STREAMING_CALLS_PER_DAY
                )

                val region =
                    appConfigRepository.watchProviderRegion.value.ifBlank { ReleaseRadarConfig.DEFAULT_REGION }
                        .uppercase()
                val userSubscriptions = appConfigRepository.userStreamingSubscriptions.first()

                for (candidate in streamingCandidates) {
                    if (candidate.title.isBlank()) {
                        continue
                    }

                    // Extra safety check on date format
                    val parsedReleaseDate = DateUtils.parseIsoDate(candidate.releaseDate)
                    if (parsedReleaseDate == null) {
                        // Malformed release date in db, mark checked to avoid spinning
                        watchlistDao.updateStreamingCheckStatus(
                            mediaId = candidate.mediaId,
                            checkEpochMs = System.currentTimeMillis(),
                            providers = "",
                            hasNotified = false
                        )
                        continue
                    }

                    try {
                        val response = tmdbApiService.getMovieWatchProviders(candidate.mediaId)
                        if (response is ApiResponse.Success) {
                            val results = response.body.results
                            val regionProviders = results?.get(region) ?: results?.get(
                                ReleaseRadarConfig.DEFAULT_REGION
                            )

                            val availableProviders = mutableListOf<RemoteProviderInfo>()
                            regionProviders?.flatRate?.let { availableProviders.addAll(it) }
                            regionProviders?.free?.let { availableProviders.addAll(it) }
                            regionProviders?.ads?.let { availableProviders.addAll(it) }

                            val matchedProviders = filterMatchedProviders(
                                availableProviders = availableProviders,
                                userSubscriptions = userSubscriptions
                            )

                            if (matchedProviders.isNotEmpty()) {
                                val providerNames = formatStreamingProviderNames(matchedProviders)
                                if (!notificationDispatched) {
                                    val title = context.getString(
                                        R.string.release_radar_streaming_title,
                                        candidate.title
                                    )
                                    val message = context.getString(
                                        R.string.release_radar_streaming_desc,
                                        providerNames
                                    )
                                    val imageUrl =
                                        candidate.posterImageUrl.ifBlank { candidate.backdropImageUrl }
                                    val deepLink = buildMovieDeepLink(candidate.mediaId)

                                    notificationManager.showReminderNotification(
                                        title = title,
                                        message = message,
                                        imageUrl = imageUrl.ifBlank { null },
                                        deepLink = deepLink
                                    )
                                    notificationDispatched = true

                                    watchlistDao.updateStreamingCheckStatus(
                                        mediaId = candidate.mediaId,
                                        checkEpochMs = System.currentTimeMillis(),
                                        providers = providerNames,
                                        hasNotified = true
                                    )
                                }
                            } else {
                                // Not available on streaming: cache timestamp to enforce 7-day cooldown
                                watchlistDao.updateStreamingCheckStatus(
                                    mediaId = candidate.mediaId,
                                    checkEpochMs = System.currentTimeMillis(),
                                    providers = "",
                                    hasNotified = false
                                )
                            }
                        }
                    } catch (_: Exception) {
                        // Transient network or serialization error for a single movie: do not crash worker
                    }
                }
            }

            ReleaseRadarResult.Success
        } catch (e: Exception) {
            ReleaseRadarResult.Retry
        }
    }

    companion object {
        fun buildMovieDeepLink(mediaId: Int): String {
            return "showtime://showtime.ssverma.in/movie/$mediaId"
        }

        fun filterMatchedProviders(
            availableProviders: List<RemoteProviderInfo>,
            userSubscriptions: Set<Int>
        ): List<RemoteProviderInfo> {
            return if (userSubscriptions.isNotEmpty()) {
                availableProviders.filter { it.providerId in userSubscriptions }
            } else {
                availableProviders
            }
        }

        fun formatStreamingProviderNames(providers: List<RemoteProviderInfo>): String {
            return providers.take(3).joinToString(", ") { it.providerName }
        }
    }
}
