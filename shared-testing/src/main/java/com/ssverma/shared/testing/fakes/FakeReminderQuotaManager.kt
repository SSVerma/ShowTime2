package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.repository.ReminderQuotaManager

class FakeReminderQuotaManager : ReminderQuotaManager {
    var extraReminderSlots: Int = 0
    var freeLimit: Int = 3
    var grantReminderPassCallCount: Int = 0
    var consumeReminderPassCallCount: Int = 0

    override suspend fun canScheduleReminder(
        currentActiveCount: Int,
        isProActive: Boolean
    ): Boolean {
        if (isProActive) return true
        if (currentActiveCount < freeLimit) return true
        return extraReminderSlots > 0
    }

    override suspend fun grantReminderPass() {
        grantReminderPassCallCount++
        extraReminderSlots++
    }

    override suspend fun consumeReminderPass(): Boolean {
        consumeReminderPassCallCount++
        if (extraReminderSlots > 0) {
            extraReminderSlots--
            return true
        }
        return false
    }
}
