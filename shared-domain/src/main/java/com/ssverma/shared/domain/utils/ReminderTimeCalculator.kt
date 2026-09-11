package com.ssverma.shared.domain.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object ReminderTimeCalculator {

    const val DEFAULT_NOTIFICATION_HOUR = 9
    const val DEFAULT_NOTIFICATION_MINUTE = 0

    /**
     * Calculates the target epoch millis for a reminder given an air date and preferred time.
     *
     * - Returns null if the air date is strictly in the past (before today).
     * - If the air date is today and the scheduled time has already passed, returns [nowMillis] + 5 seconds
     *   so the notification fires promptly today.
     * - Otherwise returns the exact epoch millis of [airDate] at [hour]:[minute] in [zoneId].
     */
    fun calculateReminderTime(
        airDate: LocalDate,
        hour: Int = DEFAULT_NOTIFICATION_HOUR,
        minute: Int = DEFAULT_NOTIFICATION_MINUTE,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        if (airDate.isBefore(today)) {
            return null
        }

        val targetZonedDateTime = airDate.atTime(LocalTime.of(hour, minute)).atZone(zoneId)
        val targetMillis = targetZonedDateTime.toInstant().toEpochMilli()

        return if (targetMillis <= nowMillis) {
            if (airDate.isEqual(today)) {
                nowMillis + 5_000L
            } else {
                null
            }
        } else {
            targetMillis
        }
    }
}
