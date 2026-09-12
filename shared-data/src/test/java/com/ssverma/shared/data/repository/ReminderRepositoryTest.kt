package com.ssverma.shared.data.repository

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.core.billing.BillingRepository
import com.ssverma.shared.data.local.db.dao.AiringReminderDao
import com.ssverma.shared.data.local.db.entity.AiringReminderEntity
import com.ssverma.shared.data.worker.AiringReminderScheduler
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.ReminderToggleResult
import com.ssverma.shared.testing.fakes.FakeReminderQuotaManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ReminderRepositoryTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockDao: AiringReminderDao = mockk(relaxed = true)
    private val mockScheduler: AiringReminderScheduler = mockk(relaxed = true)
    private val mockTmdbApiService: TmdbApiService = mockk(relaxed = true)
    private val mockAppConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val mockBillingRepository: BillingRepository = mockk(relaxed = true)
    private val fakeReminderQuotaManager = FakeReminderQuotaManager()

    private val isProFlow = MutableStateFlow(false)

    private lateinit var repository: ReminderRepositoryImpl

    @Before
    fun setUp() {
        every { mockAppConfigRepository.reminderNotificationHour } returns flowOf(9)
        every { mockAppConfigRepository.reminderNotificationMinute } returns flowOf(0)
        every { mockBillingRepository.isProActive } returns isProFlow
        fakeReminderQuotaManager.extraReminderSlots = 0
        fakeReminderQuotaManager.freeLimit = 3

        repository = ReminderRepositoryImpl(
            context = mockContext,
            airingReminderDao = mockDao,
            scheduler = mockScheduler,
            tmdbApiService = mockTmdbApiService,
            appConfigRepository = mockAppConfigRepository,
            billingRepository = mockBillingRepository,
            reminderQuotaManager = fakeReminderQuotaManager
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

    @Test
    fun `toggleMediaReminder removes reminder when already active`() = runTest {
        val existing = AiringReminderEntity(
            id = 1L,
            mediaId = 101,
            mediaType = "movie",
            reminderType = "MOVIE_RELEASE",
            mediaTitle = "Dune: Part Three",
            posterImageUrl = "/dune3.jpg",
            airDate = "2026-12-18",
            reminderTimeMillis = System.currentTimeMillis() + 100000L
        )
        coEvery { mockDao.getByMediaId(101, "movie") } returns existing

        val result = repository.toggleMediaReminder(
            mediaId = 101,
            mediaType = MediaType.Movie,
            title = "Dune: Part Three",
            posterImageUrl = "/dune3.jpg"
        )

        assertThat(result).isEqualTo(ReminderToggleResult.Removed)
        coVerify(exactly = 1) { mockDao.deleteByMediaId(101, "movie") }
        verify(exactly = 1) { mockScheduler.cancel(mockContext, 101, "movie") }
    }

    @Test
    fun `toggleMediaReminder returns QuotaExceeded when free user has reached limit`() = runTest {
        coEvery { mockDao.getByMediaId(101, "movie") } returns null
        coEvery { mockDao.getActiveCount() } returns 3
        isProFlow.value = false

        val result = repository.toggleMediaReminder(
            mediaId = 101,
            mediaType = MediaType.Movie,
            title = "Dune: Part Three",
            posterImageUrl = "/dune3.jpg"
        )

        assertThat(result).isEqualTo(ReminderToggleResult.QuotaExceeded)
    }

    @Test
    fun `toggleMediaReminder allows scheduling when user has active reminder pass even at free limit`() =
        runTest {
            coEvery { mockDao.getByMediaId(101, "movie") } returns null
            coEvery { mockDao.getActiveCount() } returnsMany listOf(3, 4)
            isProFlow.value = false
            fakeReminderQuotaManager.extraReminderSlots = 1

            val futureDate = LocalDate.now().plusMonths(2)
            val result = repository.toggleMediaReminder(
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Dune: Part Three",
                posterImageUrl = "/dune3.jpg",
                targetAirDate = futureDate
            )

            assertThat(result).isInstanceOf(ReminderToggleResult.Added::class.java)
            coVerify(exactly = 1) {
                mockDao.insert(match {
                    it.mediaId == 101 && it.mediaTitle == "Dune: Part Three" && it.mediaType == "movie"
                })
            }
            assertThat(fakeReminderQuotaManager.extraReminderSlots).isEqualTo(0)
            assertThat(fakeReminderQuotaManager.consumeReminderPassCallCount).isEqualTo(1)
        }

    @Test
    fun `toggleMediaReminder blocks subsequent scheduling once single-use pass is consumed`() =
        runTest {
            coEvery { mockDao.getByMediaId(101, "movie") } returns null
            coEvery { mockDao.getByMediaId(102, "movie") } returns null
            coEvery { mockDao.getActiveCount() } returnsMany listOf(3, 4, 4)
            isProFlow.value = false
            fakeReminderQuotaManager.extraReminderSlots = 1

            val futureDate = LocalDate.now().plusMonths(2)
            val firstResult = repository.toggleMediaReminder(
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Dune: Part Three",
                posterImageUrl = "/dune3.jpg",
                targetAirDate = futureDate
            )
            assertThat(firstResult).isInstanceOf(ReminderToggleResult.Added::class.java)
            assertThat(fakeReminderQuotaManager.extraReminderSlots).isEqualTo(0)

            val secondResult = repository.toggleMediaReminder(
                mediaId = 102,
                mediaType = MediaType.Movie,
                title = "Blade Runner 2099",
                posterImageUrl = "/br2099.jpg",
                targetAirDate = futureDate
            )
            assertThat(secondResult).isEqualTo(ReminderToggleResult.QuotaExceeded)
        }

    @Test
    fun `toggleMediaReminder schedules reminder successfully when targetAirDate is in future`() =
        runTest {
            coEvery { mockDao.getByMediaId(101, "movie") } returns null
            coEvery { mockDao.getActiveCount() } returns 1
            isProFlow.value = false

            val futureDate = LocalDate.now().plusMonths(2)
            val result = repository.toggleMediaReminder(
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Dune: Part Three",
                posterImageUrl = "/dune3.jpg",
                targetAirDate = futureDate
            )

            assertThat(result).isInstanceOf(ReminderToggleResult.Added::class.java)
            coVerify(exactly = 1) {
                mockDao.insert(match {
                    it.mediaId == 101 && it.mediaTitle == "Dune: Part Three" && it.mediaType == "movie"
                })
            }
        }

    @Test
    fun `toggleMediaReminder returns NoUpcomingSchedule when targetAirDate is in the past`() =
        runTest {
            coEvery { mockDao.getByMediaId(101, "movie") } returns null
            coEvery { mockDao.getActiveCount() } returns 0
            isProFlow.value = false

            val pastDate = LocalDate.now().minusDays(5)
            val result = repository.toggleMediaReminder(
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Old Movie",
                posterImageUrl = "/old.jpg",
                targetAirDate = pastDate
            )

            assertThat(result).isEqualTo(ReminderToggleResult.NoUpcomingSchedule)
        }
}
