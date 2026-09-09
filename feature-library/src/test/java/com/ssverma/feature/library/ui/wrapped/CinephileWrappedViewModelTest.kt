package com.ssverma.feature.library.ui.wrapped

import android.app.Activity
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.ads.quota.RewardPassStatus
import com.ssverma.shared.ads.quota.RewardPassType
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.feature.library.ui.wrapped.component.WrappedStoryStyle
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.usecase.stats.GetCinephileWrappedUseCase
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.testing.fakes.FakeBackupRepository
import com.ssverma.shared.testing.fakes.FakeCinephileMilestoneRepository
import com.ssverma.shared.testing.fakes.FakeDiaryRepository
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CinephileWrappedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDiaryRepository: FakeDiaryRepository
    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var fakeMilestoneRepository: FakeCinephileMilestoneRepository
    private lateinit var fakeBillingRepository: FakeBillingRepository
    private lateinit var fakeBackupRepository: FakeBackupRepository
    private val mockRewardManager: RewardManager = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)
    private val passStatusFlow = MutableStateFlow(RewardPassStatus())
    private lateinit var viewModel: CinephileWrappedViewModel

    @Before
    fun setUp() {
        fakeDiaryRepository = FakeDiaryRepository()
        fakeLibraryRepository = FakeLibraryRepository()
        fakeMilestoneRepository = FakeCinephileMilestoneRepository()
        fakeBillingRepository = FakeBillingRepository(initialProActive = false)
        fakeBackupRepository = FakeBackupRepository()
        every { mockRewardManager.passStatus } returns passStatusFlow

        val getCinephileWrappedUseCase = GetCinephileWrappedUseCase(
            diaryRepository = fakeDiaryRepository,
            libraryRepository = fakeLibraryRepository,
            milestoneRepository = fakeMilestoneRepository
        )

        viewModel = CinephileWrappedViewModel(
            getCinephileWrappedUseCase = getCinephileWrappedUseCase,
            billingRepository = fakeBillingRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager,
            backupRepository = fakeBackupRepository
        )
    }

    @Test
    fun `initial state loads all-time summary`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.isLoading)
        assertNotNull(state.summary)
        assertEquals(0, state.selectedYear)
        assertEquals(0, state.summary!!.totalLogged)
        assertEquals(WrappedStoryStyle.CLASSIC_VELVET, state.selectedStyle)
        assertFalse(state.isProActive)
        assertFalse(state.isPassActive)
        assertFalse(state.isWatermarkFree)
        assertFalse(state.isGateOpen)
        assertFalse(state.isExportSheetOpen)
    }

    @Test
    fun `selecting year updates state and generates share text`() = runTest {
        val date2026 = DateUtils.toMillis(LocalDate.of(2026, 6, 1))

        fakeDiaryRepository.saveDiaryEntry(
            DiaryEntry(
                id = 1,
                mediaId = 1,
                mediaType = MediaType.Movie,
                title = "Dune: Part Two",
                posterImageUrl = "/dune2.jpg",
                releaseDate = "2024-03-01",
                userRating = 5.0f,
                isRewatch = false,
                loggedAt = date2026
            )
        )

        viewModel.onSelectYear(2026)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(2026, state.selectedYear)
        assertEquals(1, state.summary?.totalLogged)
        assertEquals("Dune: Part Two", state.summary?.topRatedMedia?.firstOrNull()?.title)

        val shareText = viewModel.generateWrappedShareText(state.summary!!)
        assertTrue(shareText.contains("Dune: Part Two"))
        assertTrue(shareText.contains("ShowTime"))
    }

    @Test
    fun `selecting milestone updates selectedMilestone state`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.first()
        val firstMilestone = state.summary?.milestones?.firstOrNull()
        assertNotNull(firstMilestone)

        viewModel.onSelectMilestone(firstMilestone)
        advanceUntilIdle()

        val updatedState = viewModel.uiState.first()
        assertEquals(firstMilestone, updatedState.selectedMilestone)

        viewModel.onSelectMilestone(null)
        advanceUntilIdle()

        val clearedState = viewModel.uiState.first()
        assertEquals(null, clearedState.selectedMilestone)
    }

    @Test
    fun `generateMilestoneShareText contains milestone title and description`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.first()
        val milestone = state.summary?.milestones?.firstOrNull()
        assertNotNull(milestone)

        val shareText = viewModel.generateMilestoneShareText(milestone!!)
        assertTrue(shareText.contains(milestone.title))
        assertTrue(shareText.contains(milestone.description))
        assertTrue(shareText.contains(milestone.tier.name))
        assertTrue(shareText.contains("ShowTime"))
    }

    @Test
    fun `pro status enables watermark-free and marks isProActive true`() = runTest {
        fakeBillingRepository.setProActive(true)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.isProActive)
        assertTrue(state.isWatermarkFree)
    }

    @Test
    fun `wrapped story pass updates isPassActive and watermark-free`() = runTest {
        passStatusFlow.value = RewardPassStatus(isWrappedStoryUnlocked = true)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.isPassActive)
        assertTrue(state.isWatermarkFree)
    }

    @Test
    fun `selecting style updates selectedStyle directly for free preview`() = runTest {
        viewModel.selectStyle(WrappedStoryStyle.OLED_NOIR)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isGateOpen)
        assertEquals(WrappedStoryStyle.OLED_NOIR, state.selectedStyle)
    }

    @Test
    fun `attemptExport with pro style as free user opens gate and remembers pending style`() =
        runTest {
            viewModel.selectStyle(WrappedStoryStyle.GOLDEN_VIP)
            advanceUntilIdle()

            var exportCalled = false
            viewModel.attemptExport { exportCalled = true }
            advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertTrue(state.isGateOpen)
            assertEquals(WrappedStoryStyle.GOLDEN_VIP, state.pendingStyle)
            assertFalse(exportCalled)
        }

    @Test
    fun `attemptExport with pro style as pro user allows export without gate`() = runTest {
        fakeBillingRepository.setProActive(true)
        advanceUntilIdle()

        viewModel.selectStyle(WrappedStoryStyle.GOLDEN_VIP)
        advanceUntilIdle()

        var exportCalled = false
        viewModel.attemptExport { exportCalled = true }
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isGateOpen)
        assertTrue(exportCalled)
    }

    @Test
    fun `attemptExport with classic style as free user allows export without gate`() = runTest {
        viewModel.selectStyle(WrappedStoryStyle.CLASSIC_VELVET)
        advanceUntilIdle()

        var exportCalled = false
        viewModel.attemptExport { exportCalled = true }
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isGateOpen)
        assertTrue(exportCalled)
    }

    @Test
    fun `watchAdForWrappedPass grants pass, applies pending style, and closes gate`() = runTest {
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

        viewModel.selectStyle(WrappedStoryStyle.NEON_CYBERPUNK)
        viewModel.attemptExport {}
        advanceUntilIdle()
        assertTrue(viewModel.uiState.first().isGateOpen)

        viewModel.watchAdForWrappedPass(activity)
        advanceUntilIdle()

        coVerify { mockRewardManager.grantRewardPass(RewardPassType.CINEMA_WRAPPED_STORY) }
        val state = viewModel.uiState.first()
        assertFalse(state.isGateOpen)
        assertEquals(WrappedStoryStyle.NEON_CYBERPUNK, state.selectedStyle)
        assertTrue(state.isPassActive)
    }

    @Test
    fun `google user updates userName in uiState`() = runTest {
        fakeBackupRepository.setGoogleUser(
            com.ssverma.core.backup.model.GoogleUser(
                displayName = "Alex",
                email = "alex@test.com",
                photoUrl = null,
                idToken = "token_123",
                uid = "123"
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals("Alex", state.userName)
    }

    @Test
    fun `openExportSheet and dismissExportSheet toggle export sheet visibility`() = runTest {
        viewModel.openExportSheet()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.first().isExportSheetOpen)

        viewModel.dismissExportSheet()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.first().isExportSheetOpen)
    }
}
