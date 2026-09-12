package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import com.ssverma.shared.domain.repository.ReminderRepository
import com.ssverma.shared.domain.repository.ReminderToggleResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeReminderRepository : ReminderRepository {

    private val reminders = MutableStateFlow<Map<Pair<Int, MediaType>, AiringReminder>>(emptyMap())

    var toggleResultOverride: ReminderToggleResult? = null

    override fun getActiveReminders(): Flow<List<AiringReminder>> {
        return reminders.map { it.values.toList() }
    }

    override fun getReminderForMedia(mediaId: Int, mediaType: MediaType): Flow<AiringReminder?> {
        return reminders.map { it[mediaId to mediaType] }
    }

    override suspend fun addReminder(reminder: AiringReminder) {
        val current = reminders.value.toMutableMap()
        current[reminder.mediaId to reminder.mediaType] = reminder
        reminders.value = current
    }

    override suspend fun removeReminder(mediaId: Int, mediaType: MediaType) {
        val current = reminders.value.toMutableMap()
        current.remove(mediaId to mediaType)
        reminders.value = current
    }

    override suspend fun removeExpiredReminders() {
        // No-op in fake
    }

    override suspend fun getActiveReminderCount(): Int {
        return reminders.value.size
    }

    override suspend fun exportToIcs(): String {
        return "BEGIN:VCALENDAR\nEND:VCALENDAR"
    }

    override suspend fun toggleMediaReminder(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        targetAirDate: LocalDate?
    ): ReminderToggleResult {
        toggleResultOverride?.let { return it }

        val key = mediaId to mediaType
        val existing = reminders.value[key]
        return if (existing != null) {
            removeReminder(mediaId, mediaType)
            ReminderToggleResult.Removed
        } else {
            val reminder = AiringReminder(
                id = (reminders.value.size + 1).toLong(),
                mediaId = mediaId,
                mediaType = mediaType,
                reminderType = if (mediaType == MediaType.Movie) ReminderType.MOVIE_RELEASE else ReminderType.TV_EPISODE,
                mediaTitle = title,
                posterImageUrl = posterImageUrl,
                airDate = targetAirDate?.toString() ?: "2026-10-01",
                reminderTimeMillis = System.currentTimeMillis() + 86400000L
            )
            addReminder(reminder)
            ReminderToggleResult.Added(label = title)
        }
    }
}
