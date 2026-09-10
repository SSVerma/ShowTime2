package com.ssverma.shared.domain.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ReminderTimeCalculatorTest {

    private val testZoneId = ZoneId.of("UTC")

    @Test
    fun `calculateReminderTime returns null for past air dates`() {
        val today = LocalDate.of(2026, 9, 10)
        val yesterday = today.minusDays(1)
        val nowMillis = today.atTime(12, 0).atZone(testZoneId).toInstant().toEpochMilli()

        val result = ReminderTimeCalculator.calculateReminderTime(
            airDate = yesterday,
            hour = 9,
            minute = 0,
            zoneId = testZoneId,
            nowMillis = nowMillis
        )

        assertThat(result).isNull()
    }

    @Test
    fun `calculateReminderTime schedules future date at specified hour and minute`() {
        val today = LocalDate.of(2026, 9, 10)
        val futureDate = today.plusDays(5)
        val nowMillis = today.atTime(12, 0).atZone(testZoneId).toInstant().toEpochMilli()

        val result = ReminderTimeCalculator.calculateReminderTime(
            airDate = futureDate,
            hour = 9,
            minute = 0,
            zoneId = testZoneId,
            nowMillis = nowMillis
        )

        val expected =
            futureDate.atTime(LocalTime.of(9, 0)).atZone(testZoneId).toInstant().toEpochMilli()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `calculateReminderTime for today before scheduled time returns scheduled time`() {
        val today = LocalDate.of(2026, 9, 10)
        val nowMillis = today.atTime(7, 30).atZone(testZoneId).toInstant().toEpochMilli()

        val result = ReminderTimeCalculator.calculateReminderTime(
            airDate = today,
            hour = 9,
            minute = 0,
            zoneId = testZoneId,
            nowMillis = nowMillis
        )

        val expected =
            today.atTime(LocalTime.of(9, 0)).atZone(testZoneId).toInstant().toEpochMilli()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `calculateReminderTime for today after scheduled time returns prompt reminder`() {
        val today = LocalDate.of(2026, 9, 10)
        val nowMillis = today.atTime(14, 0).atZone(testZoneId).toInstant().toEpochMilli()

        val result = ReminderTimeCalculator.calculateReminderTime(
            airDate = today,
            hour = 9,
            minute = 0,
            zoneId = testZoneId,
            nowMillis = nowMillis
        )

        assertThat(result).isEqualTo(nowMillis + 5_000L)
    }
}
