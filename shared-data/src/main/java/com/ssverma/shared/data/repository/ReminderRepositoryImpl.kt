package com.ssverma.shared.data.repository

import android.content.Context
import com.ssverma.shared.data.local.db.dao.AiringReminderDao
import com.ssverma.shared.data.local.db.entity.AiringReminderEntity
import com.ssverma.shared.data.worker.AiringReminderScheduler
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import com.ssverma.shared.domain.repository.ReminderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val airingReminderDao: AiringReminderDao,
    private val scheduler: AiringReminderScheduler
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
}
