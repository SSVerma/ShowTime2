package com.ssverma.feature.movie.ui.details

import android.app.Activity
import android.app.Application
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.feature.movie.domain.usecase.MovieDetailsUseCase
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.testing.fakes.FakeReminderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class MovieDetailsViewModelReminderTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movieDetailsUseCase: MovieDetailsUseCase = mockk(relaxed = true)
    private val appConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val fakeReminderRepository = FakeReminderRepository()
    private val fakeBillingRepository = FakeBillingRepository()
    private val rewardManager: RewardManager = mockk(relaxed = true)
    private val rewardedAdManager: RewardedAdManager = mockk(relaxed = true)
    private val activity: Activity = mockk()

    private lateinit var viewModel: MovieDetailsViewModel

    private val upcomingMovie: Movie = mockk(relaxed = true) {
        every { id } returns 101
        every { title } returns "Upcoming Blockbuster"
        every { nextFutureReleaseDate } returns LocalDate.now().plusDays(10)
        every { posterImageUrl } returns "https://image.tmdb.org/poster.jpg"
        every { watchProviders } returns emptyMap()
    }

    @Before
    fun setUp() {
        coEvery { movieDetailsUseCase(any()) } returns Result.Success(upcomingMovie)
        every { appConfigRepository.reminderNotificationHour } returns flowOf(9)
        every { appConfigRepository.reminderNotificationMinute } returns flowOf(0)

        viewModel = MovieDetailsViewModel(
            application = mockk(relaxed = true),
            movieId = 101,
            movieDetailsUseCase = movieDetailsUseCase,
            getMediaReactionsUseCase = mockk(relaxed = true),
            toggleMediaReactionUseCase = mockk(relaxed = true),
            getDiscussionsUseCase = mockk(relaxed = true),
            postCommentUseCase = mockk(relaxed = true),
            editCommentUseCase = mockk(relaxed = true),
            reportCommentUseCase = mockk(relaxed = true),
            toggleCommentUpvoteUseCase = mockk(relaxed = true),
            deleteCommentUseCase = mockk(relaxed = true),
            getDiaryEntriesUseCase = mockk(relaxed = true),
            saveDiaryEntryUseCase = mockk(relaxed = true),
            appConfigRepository = appConfigRepository,
            affiliateRepository = mockk(relaxed = true),
            reminderRepository = fakeReminderRepository,
            billingRepository = fakeBillingRepository,
            rewardManager = rewardManager,
            rewardedAdManager = rewardedAdManager
        )
    }

    @Test
    fun `toggleReminder displays quota gate when quota is reached`() = runTest {
        coEvery { rewardManager.canScheduleReminder(any(), any()) } returns false

        viewModel.toggleReminder(upcomingMovie)
        advanceUntilIdle()

        assertTrue(viewModel.isQuotaGateVisible.value)
    }

    @Test
    fun `dismissQuotaGate hides quota gate`() = runTest {
        coEvery { rewardManager.canScheduleReminder(any(), any()) } returns false
        viewModel.toggleReminder(upcomingMovie)
        advanceUntilIdle()
        assertTrue(viewModel.isQuotaGateVisible.value)

        viewModel.dismissQuotaGate()

        assertFalse(viewModel.isQuotaGateVisible.value)
    }

    @Test
    fun `onWatchAdForReminderPass plays rewarded ad grants pass and schedules reminder`() =
        runTest {
            coEvery { rewardManager.canScheduleReminder(any(), any()) } returnsMany listOf(
                false,
                true
            )

            viewModel.toggleReminder(upcomingMovie)
            advanceUntilIdle()
            assertTrue(viewModel.isQuotaGateVisible.value)

            val onRewardSlot = slot<() -> Unit>()
            every {
                rewardedAdManager.showRewardedAdIfReady(
                    activity = activity,
                    onAdDismissed = any(),
                    onUserEarnedReward = capture(onRewardSlot)
                )
            } answers {
                onRewardSlot.captured.invoke()
            }

            viewModel.onWatchAdForReminderPass(activity, upcomingMovie)
            advanceUntilIdle()

            coVerify(exactly = 1) { rewardManager.grantReminderPass() }
            assertFalse(viewModel.isQuotaGateVisible.value)
            assertFalse(viewModel.isAdLoading.value)
            assertEquals(1, fakeReminderRepository.getActiveReminderCount())
        }
}
