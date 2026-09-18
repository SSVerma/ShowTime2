package com.ssverma.shared.ui.component.media.menu

import android.app.Activity
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import com.ssverma.shared.domain.usecase.diary.GetDiaryEntriesUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import com.ssverma.shared.domain.repository.ReminderToggleResult
import com.ssverma.shared.testing.fakes.FakeDiaryRepository
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
import com.ssverma.shared.testing.fakes.FakeReminderQuotaManager
import com.ssverma.shared.testing.fakes.FakeReminderRepository
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.usecase.reminder.RemoveAiringReminderUseCase
import com.ssverma.shared.domain.usecase.reminder.ScheduleAiringReminderUseCase
import com.ssverma.shared.domain.usecase.reminder.ScheduleReminderResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MediaOmniMenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var fakeDiaryRepository: FakeDiaryRepository
    private lateinit var fakeReminderRepository: FakeReminderRepository
    private lateinit var fakeReminderQuotaManager: FakeReminderQuotaManager
    private val appConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val rewardedAdManager: RewardedAdManager = mockk(relaxed = true)
    private val fakeBillingRepository = FakeBillingRepository()
    private lateinit var scheduleAiringReminderUseCase: ScheduleAiringReminderUseCase
    private lateinit var removeAiringReminderUseCase: RemoveAiringReminderUseCase
    private lateinit var viewModel: MediaOmniMenuViewModel

    @Before
    fun setUp() {
        fakeLibraryRepository = FakeLibraryRepository()
        fakeDiaryRepository = FakeDiaryRepository()
        fakeReminderRepository = FakeReminderRepository()
        fakeReminderQuotaManager = FakeReminderQuotaManager()
        every { appConfigRepository.reminderNotificationHour } returns flowOf(9)
        every { appConfigRepository.reminderNotificationMinute } returns flowOf(0)
        every { appConfigRepository.reminderLeadDays } returns flowOf(0)
        coEvery { appConfigRepository.updateReminderLeadDays(any()) } returns Unit
        coEvery { appConfigRepository.updateReminderNotificationTime(any(), any()) } returns Unit

        scheduleAiringReminderUseCase = ScheduleAiringReminderUseCase(
            reminderRepository = fakeReminderRepository,
            reminderQuotaManager = fakeReminderQuotaManager,
            appConfigRepository = appConfigRepository
        )
        removeAiringReminderUseCase = RemoveAiringReminderUseCase(
            reminderRepository = fakeReminderRepository
        )

        viewModel = MediaOmniMenuViewModel(
            libraryRepository = fakeLibraryRepository,
            saveDiaryEntryUseCase = SaveDiaryEntryUseCase(fakeDiaryRepository),
            getDiaryEntriesUseCase = GetDiaryEntriesUseCase(fakeDiaryRepository),
            reminderRepository = fakeReminderRepository,
            reminderQuotaManager = fakeReminderQuotaManager,
            scheduleAiringReminderUseCase = scheduleAiringReminderUseCase,
            removeAiringReminderUseCase = removeAiringReminderUseCase,
            appConfigRepository = appConfigRepository,
            rewardedAdManager = rewardedAdManager,
            billingRepository = fakeBillingRepository
        )
    }

    @Test
    fun `getDiaryEntries returns empty list when title has not been logged`() = runTest {
        val entries = viewModel.getDiaryEntries(mediaId = 42, mediaType = MediaType.Movie).first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `saving a new diary entry creates single entry and updates on second save`() = runTest {
        val newEntry = DiaryEntry(
            id = 0L,
            mediaId = 42,
            mediaType = MediaType.Movie,
            title = "Inception",
            posterImageUrl = "/inception.jpg",
            userRating = 4.5f,
            review = "Mind-bending masterpiece"
        )

        viewModel.saveDiaryEntry(newEntry)
        advanceUntilIdle()

        val savedEntries =
            viewModel.getDiaryEntries(mediaId = 42, mediaType = MediaType.Movie).first()
        assertEquals(1, savedEntries.size)
        val existingEntry = savedEntries.first()
        assertEquals("Inception", existingEntry.title)
        assertEquals(4.5f, existingEntry.userRating)
        assertEquals("Mind-bending masterpiece", existingEntry.review)
        assertTrue("Generated ID must be greater than 0", existingEntry.id > 0L)

        // Second tap / update: using existing entry's ID
        val updatedEntry = existingEntry.copy(
            userRating = 5.0f,
            review = "Even better upon rewatch!",
            isRewatch = true
        )

        viewModel.saveDiaryEntry(updatedEntry)
        advanceUntilIdle()

        val afterUpdateEntries =
            viewModel.getDiaryEntries(mediaId = 42, mediaType = MediaType.Movie).first()
        assertEquals(
            "Must not create duplicate entries for the same media",
            1,
            afterUpdateEntries.size
        )
        val finalEntry = afterUpdateEntries.first()
        assertEquals(existingEntry.id, finalEntry.id)
        assertEquals(5.0f, finalEntry.userRating)
        assertEquals("Even better upon rewatch!", finalEntry.review)
        assertTrue(finalEntry.isRewatch)
    }

    @Test
    fun `hasReminder reflects active reminder state correctly`() = runTest {
        val initialHasReminder =
            viewModel.hasReminder(mediaId = 100, mediaType = MediaType.Movie).first()
        assertFalse(initialHasReminder)

        var toggleResult: ReminderToggleResult? = null
        viewModel.toggleReminder(
            mediaId = 100,
            mediaType = MediaType.Movie,
            title = "Dune: Part Two",
            posterImageUrl = "/dune.jpg"
        ) { result ->
            toggleResult = result
        }
        advanceUntilIdle()

        assertTrue(toggleResult is ReminderToggleResult.Added)
        assertEquals("Dune: Part Two", (toggleResult as ReminderToggleResult.Added).label)

        val updatedHasReminder =
            viewModel.hasReminder(mediaId = 100, mediaType = MediaType.Movie).first()
        assertTrue(updatedHasReminder)

        // Toggle again to remove
        viewModel.toggleReminder(
            mediaId = 100,
            mediaType = MediaType.Movie,
            title = "Dune: Part Two",
            posterImageUrl = "/dune.jpg"
        ) { result ->
            toggleResult = result
        }
        advanceUntilIdle()

        assertEquals(ReminderToggleResult.Removed, toggleResult)
        val finalHasReminder =
            viewModel.hasReminder(mediaId = 100, mediaType = MediaType.Movie).first()
        assertFalse(finalHasReminder)
    }

    @Test
    fun `onWatchAdForReminderPass plays rewarded ad and grants reminder pass`() = runTest {
        val onRewardSlot = slot<() -> Unit>()
        every {
            rewardedAdManager.showRewardedAdIfReady(
                any(),
                any(),
                capture(onRewardSlot)
            )
        } answers {
            onRewardSlot.captured.invoke()
        }

        var rewardCallbackInvoked = false
        viewModel.onWatchAdForReminderPass(mockk()) {
            rewardCallbackInvoked = true
        }
        advanceUntilIdle()

        assertTrue(rewardCallbackInvoked)
        assertEquals(1, fakeReminderQuotaManager.grantReminderPassCallCount)
        assertEquals(1, fakeReminderQuotaManager.extraReminderSlots)
        assertFalse(viewModel.isAdLoading.value)
    }

    @Test
    fun `onWatchAdForReminderPass sets isAdLoading true then false after reward`() = runTest {
        val onRewardSlot = slot<() -> Unit>()
        every {
            rewardedAdManager.showRewardedAdIfReady(
                any(),
                any(),
                capture(onRewardSlot)
            )
        } answers {
            assertTrue(viewModel.isAdLoading.value)
            onRewardSlot.captured.invoke()
        }

        viewModel.onWatchAdForReminderPass(mockk()) {}
        advanceUntilIdle()

        assertFalse(viewModel.isAdLoading.value)
    }

    @Test
    fun `toggleReminder returns QuotaExceeded when reminder limit reached for non-pro user without pass`() =
        runTest {
            fakeReminderRepository.toggleResultOverride = ReminderToggleResult.QuotaExceeded

            var toggleResult: ReminderToggleResult? = null
            viewModel.toggleReminder(
                mediaId = 4,
                mediaType = MediaType.Movie,
                title = "Movie 4",
                posterImageUrl = "/poster.jpg"
            ) { result ->
                toggleResult = result
            }
            advanceUntilIdle()

            assertEquals(ReminderToggleResult.QuotaExceeded, toggleResult)
        }

    @Test
    fun `toggleReminder succeeds after watching ad when quota was previously exceeded`() = runTest {
        fakeReminderRepository.toggleResultOverride = ReminderToggleResult.QuotaExceeded

        val onRewardSlot = slot<() -> Unit>()
        every {
            rewardedAdManager.showRewardedAdIfReady(
                any(),
                any(),
                capture(onRewardSlot)
            )
        } answers {
            onRewardSlot.captured.invoke()
        }

        var toggleResult: ReminderToggleResult? = null
        viewModel.onWatchAdForReminderPass(mockk()) {
            fakeReminderRepository.toggleResultOverride = null
            viewModel.toggleReminder(
                mediaId = 4,
                mediaType = MediaType.Movie,
                title = "Movie 4",
                posterImageUrl = "/poster.jpg"
            ) { result ->
                toggleResult = result
            }
        }
        advanceUntilIdle()

        assertTrue(toggleResult is ReminderToggleResult.Added)
        val hasReminder = viewModel.hasReminder(mediaId = 4, mediaType = MediaType.Movie).first()
        assertTrue(hasReminder)
    }

    @Test
    fun `scheduleReminder schedules reminder and updates preferences`() = runTest {
        var scheduleResult: ScheduleReminderResult? = null
        val airDate = LocalDate.now().plusDays(5)

        viewModel.scheduleReminder(
            mediaId = 55,
            mediaType = MediaType.Movie,
            title = "Oppenheimer",
            posterImageUrl = "/oppenheimer.jpg",
            targetAirDate = airDate,
            leadDays = 2,
            hour = 18,
            minute = 0
        ) { result ->
            scheduleResult = result
        }
        advanceUntilIdle()

        assertTrue(scheduleResult is ScheduleReminderResult.Success)
        val hasReminder = viewModel.hasReminder(mediaId = 55, mediaType = MediaType.Movie).first()
        assertTrue(hasReminder)

        // Remove reminder
        var removeCompleted = false
        viewModel.removeReminder(mediaId = 55, mediaType = MediaType.Movie) {
            removeCompleted = true
        }
        advanceUntilIdle()

        assertTrue(removeCompleted)
        val afterRemoveReminder =
            viewModel.hasReminder(mediaId = 55, mediaType = MediaType.Movie).first()
        assertFalse(afterRemoveReminder)
    }
}

