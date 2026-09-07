package com.ssverma.feature.library.ui.taste

import android.app.Activity
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.ads.quota.RewardManager
import com.ssverma.core.ads.quota.RewardPassStatus
import com.ssverma.core.ads.quota.RewardPassType
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.usecase.recommendation.GetSmartRecommendationsUseCase
import com.ssverma.shared.domain.usecase.stats.GetTasteProfileUseCase
import com.ssverma.shared.testing.fakes.FakeDiaryRepository
import com.ssverma.shared.testing.fakes.FakeDiscoveryRepository
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasteProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDiaryRepository: FakeDiaryRepository
    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var fakeDiscoveryRepository: FakeDiscoveryRepository
    private lateinit var fakeBillingRepository: FakeBillingRepository
    private val mockRewardManager: RewardManager = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)
    private val passStatusFlow = MutableStateFlow(RewardPassStatus())
    private lateinit var viewModel: TasteProfileViewModel

    @Before
    fun setUp() {
        fakeDiaryRepository = FakeDiaryRepository()
        fakeLibraryRepository = FakeLibraryRepository()
        fakeDiscoveryRepository = FakeDiscoveryRepository()
        fakeBillingRepository = FakeBillingRepository(initialProActive = false)
        every { mockRewardManager.passStatus } returns passStatusFlow

        val getTasteProfileUseCase = GetTasteProfileUseCase(
            diaryRepository = fakeDiaryRepository,
            libraryRepository = fakeLibraryRepository
        )
        val getSmartRecommendationsUseCase = GetSmartRecommendationsUseCase(
            discoveryRepository = fakeDiscoveryRepository
        )

        viewModel = TasteProfileViewModel(
            getTasteProfileUseCase = getTasteProfileUseCase,
            getSmartRecommendationsUseCase = getSmartRecommendationsUseCase,
            billingRepository = fakeBillingRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager
        )
    }

    @Test
    fun `initial state emits default empty stats when diary is empty`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(0, state.stats.totalItemsLogged)
        assertEquals(0, state.stats.totalWatchedMinutes)
        assertEquals(DiaryFilterType.ALL, state.selectedFilter)
        assertFalse(state.isProActive)
        assertFalse(state.isPassActive)
        assertFalse(state.isGateOpen)
    }

    @Test
    fun `adding diary entries updates taste profile analytics`() = runTest {
        val entry = DiaryEntry(
            id = 1L,
            mediaId = 500,
            mediaType = MediaType.Movie,
            title = "Blade Runner 2049",
            posterImageUrl = "/br2049.jpg",
            releaseDate = "2017-10-06",
            userRating = 5.0f,
            review = "Atmospheric masterpiece",
            isRewatch = true
        )
        fakeDiaryRepository.saveDiaryEntry(entry)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(1, state.stats.totalItemsLogged)
        assertEquals(1, state.stats.totalMoviesLogged)
        assertEquals(5.0f, state.stats.averageRating, 0.01f)
        assertEquals(1, state.stats.rewatchCount)
        assertTrue(state.stats.totalWatchedMinutes > 0)
    }

    @Test
    fun `filter changes update active filter state`() = runTest {
        viewModel.setFilter(DiaryFilterType.TV_ONLY)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(DiaryFilterType.TV_ONLY, state.selectedFilter)
    }

    @Test
    fun `share text contains core stats summary`() = runTest {
        val entry = DiaryEntry(
            id = 1L,
            mediaId = 500,
            mediaType = MediaType.Movie,
            title = "Blade Runner 2049",
            posterImageUrl = "/br2049.jpg",
            releaseDate = "2017-10-06",
            userRating = 5.0f
        )
        fakeDiaryRepository.saveDiaryEntry(entry)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        val shareText = viewModel.getShareTasteText(state.stats)

        assertTrue(shareText.contains("Blade Runner 2049"))
        assertTrue(shareText.contains("5.0"))
        assertTrue(shareText.contains("ShowTime"))
    }

    @Test
    fun `pro status updates isProActive state`() = runTest {
        fakeBillingRepository.setProActive(true)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.isProActive)
    }

    @Test
    fun `taste radar reward pass updates isPassActive state`() = runTest {
        passStatusFlow.value = RewardPassStatus(isTasteAnalyticsUnlocked = true)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.isPassActive)
    }

    @Test
    fun `openGate and dismissGate manage gate visibility`() = runTest {
        viewModel.openGate()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.first().isGateOpen)

        viewModel.dismissGate()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.first().isGateOpen)
    }

    @Test
    fun `watchAdForTasteRadarPass triggers rewarded ad and grants pass`() = runTest {
        val activity: Activity = mockk()
        val rewardCallbackSlot = slot<() -> Unit>()
        every {
            mockRewardedAdManager.showRewardedAdIfReady(
                activity,
                capture(rewardCallbackSlot)
            )
        } answers {
            rewardCallbackSlot.captured.invoke()
        }

        viewModel.openGate()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.first().isGateOpen)

        viewModel.watchAdForTasteRadarPass(activity)
        advanceUntilIdle()

        coVerify { mockRewardManager.grantRewardPass(RewardPassType.TASTE_ANALYTICS_RADAR) }
        assertFalse(viewModel.uiState.first().isGateOpen)
    }
}
