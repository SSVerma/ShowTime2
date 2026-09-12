package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.reminder.AiringReminder
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

sealed interface ReminderToggleResult {
    data class Added(val label: String) : ReminderToggleResult
    data object Removed : ReminderToggleResult
    data object QuotaExceeded : ReminderToggleResult
    data object NoUpcomingSchedule : ReminderToggleResult
    data class Error(val message: String? = null) : ReminderToggleResult
}

interface ReminderRepository {
    fun getActiveReminders(): Flow<List<AiringReminder>>

    fun getReminderForMedia(mediaId: Int, mediaType: MediaType): Flow<AiringReminder?>

    suspend fun addReminder(reminder: AiringReminder)

    suspend fun removeReminder(mediaId: Int, mediaType: MediaType)

    suspend fun removeExpiredReminders()

    suspend fun getActiveReminderCount(): Int

    suspend fun exportToIcs(): String

    suspend fun toggleMediaReminder(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        targetAirDate: LocalDate? = null
    ): ReminderToggleResult
}
