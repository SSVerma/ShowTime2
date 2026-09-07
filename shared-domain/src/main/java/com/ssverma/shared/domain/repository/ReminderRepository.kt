package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.reminder.AiringReminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getActiveReminders(): Flow<List<AiringReminder>>

    fun getReminderForMedia(mediaId: Int, mediaType: MediaType): Flow<AiringReminder?>

    suspend fun addReminder(reminder: AiringReminder)

    suspend fun removeReminder(mediaId: Int, mediaType: MediaType)

    suspend fun removeExpiredReminders()

    suspend fun getActiveReminderCount(): Int

    suspend fun exportToIcs(): String
}
