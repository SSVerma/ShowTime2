package com.ssverma.shared.domain.repository

interface ReminderQuotaManager {
    suspend fun canScheduleReminder(currentActiveCount: Int, isProActive: Boolean): Boolean
    suspend fun grantReminderPass()
    suspend fun consumeReminderPass(): Boolean
}
