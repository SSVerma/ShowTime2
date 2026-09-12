package com.ssverma.showtime.calendar

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import java.util.TimeZone
import org.junit.Test

class CalendarIntentHelperTest {

    @Test
    fun buildCalendarEventData_movieReminder_buildsCorrectEventData() {
        val reminder = AiringReminder(
            id = 1L,
            mediaId = 101,
            mediaType = MediaType.Movie,
            reminderType = ReminderType.MOVIE_RELEASE,
            mediaTitle = "Dune: Part Two",
            posterImageUrl = "/dune.jpg",
            airDate = "2026-11-01",
            reminderTimeMillis = 1730419200000L,
            providerName = "Max"
        )

        val eventData = CalendarIntentHelper.buildCalendarEventData(reminder)

        assertThat(eventData.title).isEqualTo("Dune: Part Two")
        assertThat(eventData.description).contains("Dune: Part Two")
        assertThat(eventData.description).contains("Streaming on: Max")
        assertThat(eventData.description).contains("Set via ShowTime")
        assertThat(eventData.description).doesNotContain("Season")
        assertThat(eventData.startTimeMillis).isEqualTo(1730419200000L)
        assertThat(eventData.endTimeMillis).isEqualTo(1730419200000L + 3600000L)
        assertThat(eventData.timeZone).isEqualTo(TimeZone.getDefault().id)
    }

    @Test
    fun buildCalendarEventData_tvEpisodeReminder_includesSeasonAndEpisodeInDescription() {
        val reminder = AiringReminder(
            id = 2L,
            mediaId = 202,
            mediaType = MediaType.Tv,
            reminderType = ReminderType.TV_EPISODE,
            mediaTitle = "Severance",
            posterImageUrl = "/severance.jpg",
            seasonNumber = 2,
            episodeNumber = 1,
            episodeTitle = "Hello, Kier",
            airDate = "2026-12-15",
            reminderTimeMillis = 1734220800000L,
            providerName = "Apple TV+"
        )

        val eventData = CalendarIntentHelper.buildCalendarEventData(reminder)

        assertThat(eventData.title).isEqualTo("Severance S2E1")
        assertThat(eventData.description).contains("Severance - Season 2 Episode 1: \"Hello, Kier\"")
        assertThat(eventData.description).contains("Streaming on: Apple TV+")
        assertThat(eventData.description).contains("Set via ShowTime")
    }

    @Test
    fun buildCalendarEventData_tvEpisodeWithoutEpisodeTitle_formatsSeasonAndEpisodeCorrectly() {
        val reminder = AiringReminder(
            id = 3L,
            mediaId = 203,
            mediaType = MediaType.Tv,
            reminderType = ReminderType.TV_EPISODE,
            mediaTitle = "House of the Dragon",
            posterImageUrl = "/hotd.jpg",
            seasonNumber = 3,
            episodeNumber = 4,
            episodeTitle = null,
            airDate = "2026-08-10",
            reminderTimeMillis = 1723248000000L,
            providerName = "HBO Max"
        )

        val eventData = CalendarIntentHelper.buildCalendarEventData(reminder)

        assertThat(eventData.title).isEqualTo("House of the Dragon S3E4")
        assertThat(eventData.description).contains("House of the Dragon - Season 3 Episode 4")
        assertThat(eventData.description).doesNotContain("null")
        assertThat(eventData.description).contains("Streaming on: HBO Max")
    }

    @Test
    fun buildCalendarEventData_customDuration_calculatesCorrectEndTime() {
        val reminder = AiringReminder(
            id = 4L,
            mediaId = 404,
            mediaType = MediaType.Movie,
            reminderType = ReminderType.MOVIE_RELEASE,
            mediaTitle = "Oppenheimer",
            posterImageUrl = "/oppenheimer.jpg",
            airDate = "2026-10-15",
            reminderTimeMillis = 1728950400000L
        )

        val twoHours = 7200000L
        val eventData =
            CalendarIntentHelper.buildCalendarEventData(reminder, defaultDurationMillis = twoHours)

        assertThat(eventData.endTimeMillis).isEqualTo(reminder.reminderTimeMillis + twoHours)
    }

    @Test
    fun buildCalendarEventData_withoutProvider_omitsStreamingLine() {
        val reminder = AiringReminder(
            id = 5L,
            mediaId = 505,
            mediaType = MediaType.Movie,
            reminderType = ReminderType.MOVIE_RELEASE,
            mediaTitle = "The Batman Part II",
            posterImageUrl = "/batman.jpg",
            airDate = "2026-10-02",
            reminderTimeMillis = 1727827200000L,
            providerName = null
        )

        val eventData = CalendarIntentHelper.buildCalendarEventData(reminder)

        assertThat(eventData.description).doesNotContain("Streaming on:")
        assertThat(eventData.description).startsWith("The Batman Part II\n\nSet via ShowTime")
    }
}
