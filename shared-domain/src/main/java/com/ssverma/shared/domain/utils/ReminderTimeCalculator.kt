package com.ssverma.shared.domain.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object ReminderTimeCalculator {

    const val DEFAULT_NOTIFICATION_HOUR = 9
    const val DEFAULT_NOTIFICATION_MINUTE = 0

    /**
     * Calculates the target epoch millis for a reminder given an air date, lead days, and preferred time.
     *
     * - Returns null if the air date is strictly in the past (before today).
     * - If [leadDays] > 0, calculates the target date as [airDate] minus [leadDays]. If that target date has
     *   already passed but [airDate] is today or in the future, it falls back to notifying today.
     * - If the target notification time has already passed:
     *   - If [allowSameDayFallback] is true and target date is today, returns [nowMillis] + 5 seconds so
     *     the notification fires promptly today.
     *   - Otherwise returns null.
     * - Otherwise returns the exact epoch millis of the target notification date and time in [zoneId].
     */
    fun calculateReminderTime(
        airDate: LocalDate,
        hour: Int = DEFAULT_NOTIFICATION_HOUR,
        minute: Int = DEFAULT_NOTIFICATION_MINUTE,
        leadDays: Int = 0,
        allowSameDayFallback: Boolean = true,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        if (airDate.isBefore(today)) {
            return null
        }

        val reminderDate = airDate.minusDays(leadDays.coerceAtLeast(0).toLong())
        val effectiveDate = if (reminderDate.isBefore(today)) today else reminderDate

        val targetZonedDateTime = effectiveDate.atTime(LocalTime.of(hour, minute)).atZone(zoneId)
        val targetMillis = targetZonedDateTime.toInstant().toEpochMilli()

        return if (targetMillis <= nowMillis) {
            if (allowSameDayFallback && effectiveDate.isEqual(today)) {
                nowMillis + 5_000L
            } else {
                null
            }
        } else {
            targetMillis
        }
    }
}
