package com.ssverma.shared.data.repository

import android.content.Context
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.data.local.db.dao.AiringReminderDao
import com.ssverma.shared.data.local.db.entity.AiringReminderEntity
import com.ssverma.shared.data.worker.AiringReminderScheduler
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.ReminderQuotaManager
import com.ssverma.shared.domain.repository.ReminderRepository
import com.ssverma.shared.domain.repository.ReminderToggleResult
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.ReminderTimeCalculator
import com.ssverma.shared.domain.utils.formatLocally
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val airingReminderDao: AiringReminderDao,
    private val scheduler: AiringReminderScheduler,
    private val tmdbApiService: TmdbApiService,
    private val appConfigRepository: AppConfigRepository,
    private val billingRepository: BillingRepository,
    private val reminderQuotaManager: ReminderQuotaManager
) : ReminderRepository {

    override fun getActiveReminders(): Flow<List<AiringReminder>> {
        return airingReminderDao.getAllActiveFlow().map { list ->
            list.map { it.asDomainModel() }
        }
    }

    override fun getReminderForMedia(
        mediaId: Int,
        mediaType: MediaType
    ): Flow<AiringReminder?> {
        val typeStr = mediaType.toStorageString()
        return airingReminderDao.getByMediaIdFlow(mediaId, typeStr).map { entity ->
            entity?.asDomainModel()
        }
    }

    override suspend fun addReminder(reminder: AiringReminder) {
        val entity = reminder.asEntity()
        airingReminderDao.insert(entity)

        val isPro = billingRepository.isProActive.value
        val activeCount = airingReminderDao.getActiveCount()
        if (!isPro && activeCount > FREE_REMINDERS_LIMIT) {
            reminderQuotaManager.consumeReminderPass()
        }

        val delayMillis = reminder.reminderTimeMillis - System.currentTimeMillis()
        scheduler.schedule(
            context = context,
            mediaId = reminder.mediaId,
            mediaType = reminder.mediaType.toStorageString(),
            mediaTitle = reminder.mediaTitle,
            posterImageUrl = reminder.posterImageUrl,
            seasonNumber = reminder.seasonNumber,
            episodeNumber = reminder.episodeNumber,
            episodeTitle = reminder.episodeTitle,
            providerName = reminder.providerName,
            delayMillis = delayMillis
        )
    }

    override suspend fun removeReminder(mediaId: Int, mediaType: MediaType) {
        val typeStr = mediaType.toStorageString()
        airingReminderDao.deleteByMediaId(mediaId, typeStr)
        scheduler.cancel(context, mediaId, typeStr)
    }

    override suspend fun removeExpiredReminders() {
        airingReminderDao.deleteExpired(System.currentTimeMillis())
    }

    override suspend fun getActiveReminderCount(): Int {
        return airingReminderDao.getActiveCount()
    }

    override suspend fun exportToIcs(): String {
        val reminders = airingReminderDao.getAllActive().map { it.asDomainModel() }
        val sb = StringBuilder()
        sb.append("BEGIN:VCALENDAR\r\n")
        sb.append("VERSION:2.0\r\n")
        sb.append("PRODID:-//ShowTime//Cinephile Airing Reminders//EN\r\n")
        sb.append("CALSCALE:GREGORIAN\r\n")
        sb.append("METHOD:PUBLISH\r\n")

        val utcFormatter =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"))
        val nowIso = utcFormatter.format(Instant.now())

        for (reminder in reminders) {
            val startIso = utcFormatter.format(Instant.ofEpochMilli(reminder.reminderTimeMillis))
            val endIso =
                utcFormatter.format(Instant.ofEpochMilli(reminder.reminderTimeMillis + 3600000L))

            sb.append("BEGIN:VEVENT\r\n")
            sb.append("UID:showtime-reminder-${reminder.mediaType.toStorageString()}-${reminder.mediaId}@ssverma.in\r\n")
            sb.append("DTSTAMP:$nowIso\r\n")
            sb.append("DTSTART:$startIso\r\n")
            sb.append("DTEND:$endIso\r\n")
            sb.append("SUMMARY:${reminder.displayLabel}\r\n")

            val desc = buildString {
                when (reminder.reminderType) {
                    ReminderType.TV_EPISODE -> {
                        append("New TV episode airing today on ShowTime.")
                        if (!reminder.episodeTitle.isNullOrBlank()) {
                            append(" Episode: \\\"${reminder.episodeTitle}\\\".")
                        }
                    }

                    ReminderType.MOVIE_RELEASE -> {
                        append("Movie theatrical/digital release today on ShowTime.")
                    }
                }
                if (!reminder.providerName.isNullOrBlank()) {
                    append(" Available on ${reminder.providerName}.")
                }
            }
            sb.append("DESCRIPTION:$desc\r\n")
            sb.append("STATUS:CONFIRMED\r\n")
            sb.append("END:VEVENT\r\n")
        }

        sb.append("END:VCALENDAR\r\n")
        return sb.toString()
    }

    override suspend fun toggleMediaReminder(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        targetAirDate: LocalDate?
    ): ReminderToggleResult {
        val typeStr = mediaType.toStorageString()
        val existing = airingReminderDao.getByMediaId(mediaId, typeStr)
        if (existing != null) {
            removeReminder(mediaId, mediaType)
            return ReminderToggleResult.Removed
        }

        val isPro = billingRepository.isProActive.value
        val activeCount = airingReminderDao.getActiveCount()
        if (!reminderQuotaManager.canScheduleReminder(activeCount, isPro)) {
            return ReminderToggleResult.QuotaExceeded
        }

        var resolvedAirDate: LocalDate? = targetAirDate
        val reminderType: ReminderType
        var seasonNum: Int? = null
        var episodeNum: Int? = null
        var epTitle: String? = null
        var displayAirDateStr: String? = null

        if (mediaType == MediaType.Movie) {
            reminderType = ReminderType.MOVIE_RELEASE
            if (resolvedAirDate == null) {
                val response = try {
                    tmdbApiService.getMovieDetails(
                        movieId = mediaId,
                        queryMap = mapOf("append_to_response" to "release_dates")
                    )
                } catch (e: Exception) {
                    null
                }
                if (response !is ApiResponse.Success) {
                    return ReminderToggleResult.Error("Unable to fetch movie release details")
                }
                val remoteMovie = response.body
                val parsedDate = DateUtils.parseIsoDate(remoteMovie.releaseDate)
                val today = LocalDate.now()
                val futureDates = remoteMovie.releaseDates?.results
                    ?.flatMap { it.releaseDates.orEmpty() }
                    ?.mapNotNull { DateUtils.parseIsoDate(it.releaseDate?.substringBefore("T")) }
                    ?.filter { it.isAfter(today) || it.isEqual(today) }
                resolvedAirDate = futureDates?.minOrNull() ?: parsedDate
            }
            if (resolvedAirDate == null || resolvedAirDate.isBefore(LocalDate.now())) {
                return ReminderToggleResult.NoUpcomingSchedule
            }
            displayAirDateStr = resolvedAirDate.formatLocally() ?: resolvedAirDate.toString()
        } else {
            reminderType = ReminderType.TV_EPISODE
            val response = try {
                tmdbApiService.getTvShowDetails(
                    tvShowId = mediaId,
                    queryMap = emptyMap()
                )
            } catch (e: Exception) {
                null
            }
            if (response !is ApiResponse.Success) {
                return ReminderToggleResult.Error("Unable to fetch TV show air details")
            }
            val remoteTvShow = response.body
            val nextEpisode = remoteTvShow.nextEpisodeToAir
            val parsedDate = DateUtils.parseIsoDate(nextEpisode?.airDate)
            if (nextEpisode == null || parsedDate == null || parsedDate.isBefore(LocalDate.now())) {
                return ReminderToggleResult.NoUpcomingSchedule
            }
            resolvedAirDate = parsedDate
            seasonNum = nextEpisode.seasonNumber
            episodeNum = nextEpisode.episodeNumber
            epTitle = nextEpisode.title?.takeIf { it.isNotBlank() }
            displayAirDateStr = parsedDate.formatLocally() ?: nextEpisode.airDate.orEmpty()
        }

        val hour = appConfigRepository.reminderNotificationHour.first()
        val minute = appConfigRepository.reminderNotificationMinute.first()
        val reminderTime = ReminderTimeCalculator.calculateReminderTime(
            airDate = resolvedAirDate,
            hour = hour,
            minute = minute
        ) ?: return ReminderToggleResult.NoUpcomingSchedule

        val reminder = AiringReminder(
            mediaId = mediaId,
            mediaType = mediaType,
            reminderType = reminderType,
            mediaTitle = title,
            posterImageUrl = posterImageUrl,
            seasonNumber = seasonNum,
            episodeNumber = episodeNum,
            episodeTitle = epTitle,
            airDate = displayAirDateStr,
            reminderTimeMillis = reminderTime
        )
        addReminder(reminder)
        return ReminderToggleResult.Added(reminder.displayLabel)
    }

    private fun AiringReminderEntity.asDomainModel(): AiringReminder {
        return AiringReminder(
            id = id,
            mediaId = mediaId,
            mediaType = mediaType.toMediaType(),
            reminderType = try {
                ReminderType.valueOf(reminderType)
            } catch (_: Exception) {
                ReminderType.MOVIE_RELEASE
            },
            mediaTitle = mediaTitle,
            posterImageUrl = posterImageUrl,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            airDate = airDate,
            reminderTimeMillis = reminderTimeMillis,
            providerName = providerName,
            isActive = isActive,
            createdAt = createdAt
        )
    }

    private fun AiringReminder.asEntity(): AiringReminderEntity {
        return AiringReminderEntity(
            id = id,
            mediaId = mediaId,
            mediaType = mediaType.toStorageString(),
            reminderType = reminderType.name,
            mediaTitle = mediaTitle,
            posterImageUrl = posterImageUrl,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            airDate = airDate,
            reminderTimeMillis = reminderTimeMillis,
            providerName = providerName,
            isActive = isActive,
            createdAt = createdAt
        )
    }

    private fun MediaType.toStorageString(): String {
        return when (this) {
            MediaType.Tv -> "tv"
            else -> "movie"
        }
    }

    private fun String.toMediaType(): MediaType {
        return when (this.lowercase()) {
            "tv" -> MediaType.Tv
            else -> MediaType.Movie
        }
    }

    companion object {
        const val FREE_REMINDERS_LIMIT = 3
    }
}
