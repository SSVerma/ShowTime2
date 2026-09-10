package com.ssverma.shared.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.data.local.db.dao.AiringReminderDao
import com.ssverma.shared.data.local.db.dao.ShowWatchProgressDao
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.ReminderRepository
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.ReminderTimeCalculator
import com.ssverma.shared.domain.utils.formatLocally
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderWorkerEntryPoint {
    fun airingReminderDao(): AiringReminderDao
    fun tmdbApiService(): TmdbApiService
    fun reminderRepository(): ReminderRepository
    fun appConfigRepository(): AppConfigRepository
    fun showWatchProgressDao(): ShowWatchProgressDao
}

class AiringReminderRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                ReminderWorkerEntryPoint::class.java
            )
            val airingReminderDao = entryPoint.airingReminderDao()
            val tmdbApiService = entryPoint.tmdbApiService()
            val reminderRepository = entryPoint.reminderRepository()
            val appConfigRepository = entryPoint.appConfigRepository()
            val showWatchProgressDao = entryPoint.showWatchProgressDao()

            val now = System.currentTimeMillis()

            // 1. Rolling TV reminders: advance expired TV reminders to the next scheduled episode
            val activeReminders = airingReminderDao.getAllActive()
            val expiredTvReminders = activeReminders.filter {
                it.mediaType == "tv" && it.reminderTimeMillis < now
            }

            val hour = appConfigRepository.reminderNotificationHour.first()
            val minute = appConfigRepository.reminderNotificationMinute.first()

            for (expiredReminder in expiredTvReminders) {
                try {
                    val response = tmdbApiService.getTvShowDetails(
                        tvShowId = expiredReminder.mediaId,
                        queryMap = emptyMap()
                    )
                    if (response is ApiResponse.Success) {
                        val nextEpisode = response.body.nextEpisodeToAir
                        val nextAirDate = DateUtils.parseIsoDate(nextEpisode?.airDate)
                        if (nextEpisode != null && nextAirDate != null) {
                            val nextReminderTime = ReminderTimeCalculator.calculateReminderTime(
                                airDate = nextAirDate,
                                hour = hour,
                                minute = minute
                            )
                            if (nextReminderTime != null) {
                                val nextReminder = AiringReminder(
                                    mediaId = expiredReminder.mediaId,
                                    mediaType = MediaType.Tv,
                                    reminderType = ReminderType.TV_EPISODE,
                                    mediaTitle = expiredReminder.mediaTitle,
                                    posterImageUrl = expiredReminder.posterImageUrl,
                                    seasonNumber = nextEpisode.seasonNumber,
                                    episodeNumber = nextEpisode.episodeNumber,
                                    episodeTitle = nextEpisode.title.takeIf { !it.isNullOrBlank() },
                                    airDate = nextAirDate.formatLocally()
                                        ?: nextEpisode.airDate.orEmpty(),
                                    reminderTimeMillis = nextReminderTime,
                                    providerName = expiredReminder.providerName
                                )
                                reminderRepository.addReminder(nextReminder)
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }

            // 2. Clean up any remaining expired reminders (e.g. past movie releases or concluded TV shows)
            airingReminderDao.deleteExpired(now)

            // 3. Smart auto-track for caught-up shows (from local watch progress)
            try {
                val caughtUpShows = showWatchProgressDao.getAllProgress().filter {
                    it.totalAired > 0 && it.totalCompleted >= it.totalAired
                }

                for (show in caughtUpShows) {
                    val currentCount = airingReminderDao.getActiveCount()
                    if (currentCount >= 3) break

                    val existingReminder = airingReminderDao.getByMediaId(show.showId, "tv")
                    if (existingReminder == null) {
                        val response = tmdbApiService.getTvShowDetails(show.showId, emptyMap())
                        if (response is ApiResponse.Success) {
                            val nextEpisode = response.body.nextEpisodeToAir
                            val nextAirDate = DateUtils.parseIsoDate(nextEpisode?.airDate)
                            if (nextEpisode != null && nextAirDate != null) {
                                val scheduledTime = ReminderTimeCalculator.calculateReminderTime(
                                    airDate = nextAirDate,
                                    hour = hour,
                                    minute = minute
                                )
                                if (scheduledTime != null) {
                                    val autoReminder = AiringReminder(
                                        mediaId = show.showId,
                                        mediaType = MediaType.Tv,
                                        reminderType = ReminderType.TV_EPISODE,
                                        mediaTitle = show.showTitle,
                                        posterImageUrl = show.showPosterPath.orEmpty(),
                                        seasonNumber = nextEpisode.seasonNumber,
                                        episodeNumber = nextEpisode.episodeNumber,
                                        episodeTitle = nextEpisode.title.takeIf { !it.isNullOrBlank() },
                                        airDate = nextAirDate.formatLocally()
                                            ?: nextEpisode.airDate.orEmpty(),
                                        reminderTimeMillis = scheduledTime
                                    )
                                    reminderRepository.addReminder(autoReminder)
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "daily_airing_reminder_refresh"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AiringReminderRefreshWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
