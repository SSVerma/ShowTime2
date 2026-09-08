package com.ssverma.feature.library.ui.receipt

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.ads.quota.RewardManager
import com.ssverma.core.ads.quota.RewardPassStatus
import com.ssverma.core.ads.quota.RewardPassType
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.feature.library.domain.model.ReceiptSource
import com.ssverma.feature.library.domain.model.ReceiptStyle
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.testing.fakes.FakeBackupRepository
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CinemaReceiptViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var fakeBillingRepository: FakeBillingRepository
    private lateinit var fakeBackupRepository: FakeBackupRepository
    private val mockRewardManager: RewardManager = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)
    private val passStatusFlow = MutableStateFlow(RewardPassStatus())
    private lateinit var viewModel: CinemaReceiptViewModel

    @Before
    fun setUp() {
        fakeLibraryRepository = FakeLibraryRepository()
        fakeBillingRepository = FakeBillingRepository(initialProActive = false)
        fakeBackupRepository = FakeBackupRepository()
        every { mockRewardManager.passStatus } returns passStatusFlow
        coEvery { mockRewardManager.grantRewardPass(RewardPassType.WATERMARK_FREE_RECEIPT) } answers {
            passStatusFlow.value = RewardPassStatus(
                isReceiptWatermarkFreeUnlocked = true,
                receiptWatermarkFreeExpiryTimestamp = System.currentTimeMillis() + 86400000
            )
        }

        viewModel = CinemaReceiptViewModel(
            libraryRepository = fakeLibraryRepository,
            billingRepository = fakeBillingRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager,
            backupRepository = fakeBackupRepository
        )
    }

    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
    }

    @Test
    fun `initial state has correct default values for free user`() = runTest {
        collectUiState()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.selectedStyle).isEqualTo(ReceiptStyle.THERMAL)
        assertThat(state.selectedSource).isEqualTo(ReceiptSource.HISTORY)
        assertThat(state.isProActive).isFalse()
        assertThat(state.isPassActive).isFalse()
        assertThat(state.theaterName).isEqualTo("SHOWTIME CINEMA")
        assertThat(state.collectorName).isEqualTo("SHOWTIME CINEPHILE")
        assertThat(state.isGateOpen).isFalse()
    }

    @Test
    fun `free user can select and preview golden pass style`() = runTest {
        collectUiState()
        advanceUntilIdle()
        viewModel.selectStyle(ReceiptStyle.GOLDEN_PASS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.selectedStyle).isEqualTo(ReceiptStyle.GOLDEN_PASS)
        assertThat(state.isGateOpen).isFalse()
    }

    @Test
    fun `attemptExport on thermal style succeeds for free user`() = runTest {
        collectUiState()
        advanceUntilIdle()
        viewModel.selectStyle(ReceiptStyle.THERMAL)

        var allowedCalled = false
        viewModel.attemptExport { allowedCalled = true }

        assertThat(allowedCalled).isTrue()
        assertThat(viewModel.uiState.value.isGateOpen).isFalse()
    }

    @Test
    fun `free user attemptExport on golden pass triggers gate`() = runTest {
        collectUiState()
        advanceUntilIdle()
        viewModel.selectStyle(ReceiptStyle.GOLDEN_PASS)

        var allowedCalled = false
        viewModel.attemptExport { allowedCalled = true }

        assertThat(allowedCalled).isFalse()
        assertThat(viewModel.uiState.value.isGateOpen).isTrue()
        assertThat(viewModel.uiState.value.pendingStyle).isEqualTo(ReceiptStyle.GOLDEN_PASS)
    }

    @Test
    fun `pro user attemptExport on golden pass succeeds directly`() = runTest {
        collectUiState()
        fakeBillingRepository.setProActive(true)
        advanceUntilIdle()

        viewModel.selectStyle(ReceiptStyle.GOLDEN_PASS)

        var allowedCalled = false
        viewModel.attemptExport { allowedCalled = true }

        assertThat(allowedCalled).isTrue()
        assertThat(viewModel.uiState.value.isGateOpen).isFalse()
    }

    @Test
    fun `watching rewarded ad unlocks pass and applies pending style`() = runTest {
        collectUiState()
        advanceUntilIdle()
        viewModel.selectStyle(ReceiptStyle.CYBERPUNK)
        viewModel.attemptExport { }
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isGateOpen).isTrue()

        val onRewardSlot = slot<() -> Unit>()
        every {
            mockRewardedAdManager.showRewardedAdIfReady(any(), capture(onRewardSlot))
        } answers {
            onRewardSlot.captured.invoke()
        }

        val mockActivity: Activity = mockk(relaxed = true)
        viewModel.watchAdForWatermarkFreePass(mockActivity)
        advanceUntilIdle()

        coVerify { mockRewardManager.grantRewardPass(RewardPassType.WATERMARK_FREE_RECEIPT) }
        val state = viewModel.uiState.value
        assertThat(state.isPassActive).isTrue()
        assertThat(state.selectedStyle).isEqualTo(ReceiptStyle.CYBERPUNK)
        assertThat(state.isGateOpen).isFalse()
    }

    @Test
    fun `updatePersonalization updates theater and collector names and closes edit dialog`() =
        runTest {
            collectUiState()
            advanceUntilIdle()

            viewModel.setEditPersonalizationOpen(true)
            assertThat(viewModel.uiState.value.isEditPersonalizationOpen).isTrue()

            viewModel.updatePersonalization("Shyam's Midnight Cinema", "Shyam")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.theaterName).isEqualTo("Shyam's Midnight Cinema")
            assertThat(state.collectorName).isEqualTo("Shyam")
            assertThat(state.isEditPersonalizationOpen).isFalse()
            assertThat(state.snapshot?.theaterName).isEqualTo("SHYAM'S MIDNIGHT CINEMA")
            assertThat(state.snapshot?.collectorName).isEqualTo("SHYAM")
        }

    @Test
    fun `selectSource THIS_YEAR filters items and generates snapshot`() = runTest {
        collectUiState()
        fakeLibraryRepository.logWatchHistory(
            mediaId = 101,
            mediaType = MediaType.Movie,
            title = "Inception",
            posterImageUrl = "/inc.jpg",
            voteAvg = 8.8f
        )
        advanceUntilIdle()

        viewModel.selectSource(ReceiptSource.THIS_YEAR)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.selectedSource).isEqualTo(ReceiptSource.THIS_YEAR)
        assertThat(state.snapshot).isNotNull()
        assertThat(state.snapshot?.title).isEqualTo("This Year")
        assertThat(state.snapshot?.items?.any { it.id == 101 }).isTrue()
    }

    @Test
    fun `dismissGate clears pending style and closes gate`() = runTest {
        collectUiState()
        advanceUntilIdle()
        viewModel.selectStyle(ReceiptStyle.GOLDEN_PASS)
        viewModel.attemptExport { }
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isGateOpen).isTrue()

        viewModel.dismissGate()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isGateOpen).isFalse()
        assertThat(viewModel.uiState.value.pendingStyle).isNull()
    }
}
