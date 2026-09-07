package com.ssverma.shared.data.repository

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.data.local.db.dao.AiringReminderDao
import com.ssverma.shared.data.local.db.entity.AiringReminderEntity
import com.ssverma.shared.data.worker.AiringReminderScheduler
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ReminderRepositoryTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockDao: AiringReminderDao = mockk(relaxed = true)
    private val mockScheduler: AiringReminderScheduler = mockk(relaxed = true)

    private lateinit var repository: ReminderRepositoryImpl

    @Before
    fun setUp() {
        repository = ReminderRepositoryImpl(
            context = mockContext,
            airingReminderDao = mockDao,
            scheduler = mockScheduler
        )
    }

    @Test
    fun `addReminder inserts entity into dao and schedules WorkManager job`() = runTest {
        val reminder = AiringReminder(
            mediaId = 101,
            mediaType = MediaType.Movie,
            reminderType = ReminderType.MOVIE_RELEASE,
            mediaTitle = "Dune: Part Three",
            posterImageUrl = "/dune3.jpg",
            airDate = "2026-12-18",
            reminderTimeMillis = System.currentTimeMillis() + 100000L
        )

        repository.addReminder(reminder)

        coVerify {
            mockDao.insert(match {
                it.mediaId == 101 && it.mediaTitle == "Dune: Part Three" && it.mediaType == "movie"
            })
        }

        verify {
            mockScheduler.schedule(
                context = mockContext,
                mediaId = 101,
                mediaType = "movie",
                mediaTitle = "Dune: Part Three",
                posterImageUrl = "/dune3.jpg",
                seasonNumber = null,
                episodeNumber = null,
                episodeTitle = null,
                providerName = null,
                delayMillis = any()
            )
        }
    }

    @Test
    fun `removeReminder deletes from dao and cancels WorkManager job`() = runTest {
        repository.removeReminder(mediaId = 101, mediaType = MediaType.Movie)

        coVerify {
            mockDao.deleteByMediaId(101, "movie")
        }

        verify {
            mockScheduler.cancel(mockContext, 101, "movie")
        }
    }

    @Test
    fun `getActiveReminders maps entities to domain models correctly`() = runTest {
        val entities = listOf(
            AiringReminderEntity(
                id = 1L,
                mediaId = 201,
                mediaType = "tv",
                reminderType = "TV_EPISODE",
                mediaTitle = "Severance",
                posterImageUrl = "/severance.jpg",
                seasonNumber = 2,
                episodeNumber = 4,
                episodeTitle = "The Way We Were",
                airDate = "2026-09-15",
                reminderTimeMillis = 1757926800000L,
                providerName = "Apple TV+"
            )
        )
        every { mockDao.getAllActiveFlow() } returns flowOf(entities)

        val reminders = repository.getActiveReminders().first()
        assertThat(reminders).hasSize(1)

        val reminder = reminders.first()
        assertThat(reminder.mediaId).isEqualTo(201)
        assertThat(reminder.mediaType).isEqualTo(MediaType.Tv)
        assertThat(reminder.reminderType).isEqualTo(ReminderType.TV_EPISODE)
        assertThat(reminder.displayLabel).isEqualTo("Severance S2E4")
        assertThat(reminder.providerName).isEqualTo("Apple TV+")
    }

    @Test
    fun `exportToIcs produces valid VCALENDAR with VEVENT entries`() = runTest {
        val entities = listOf(
            AiringReminderEntity(
                id = 1L,
                mediaId = 201,
                mediaType = "tv",
                reminderType = "TV_EPISODE",
                mediaTitle = "Severance",
                posterImageUrl = "/severance.jpg",
                seasonNumber = 2,
                episodeNumber = 4,
                episodeTitle = "The Way We Were",
                airDate = "2026-09-15",
                reminderTimeMillis = 1757926800000L,
                providerName = "Apple TV+"
            )
        )
        coEvery { mockDao.getAllActive() } returns entities

        val ics = repository.exportToIcs()
        assertThat(ics).contains("BEGIN:VCALENDAR")
        assertThat(ics).contains("VERSION:2.0")
        assertThat(ics).contains("BEGIN:VEVENT")
        assertThat(ics).contains("SUMMARY:Severance S2E4")
        assertThat(ics).contains("Apple TV+")
        assertThat(ics).contains("END:VEVENT")
        assertThat(ics).contains("END:VCALENDAR")
    }
}
