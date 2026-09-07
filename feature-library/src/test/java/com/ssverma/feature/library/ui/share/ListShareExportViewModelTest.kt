package com.ssverma.feature.library.ui.share

import android.app.Activity
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.ads.quota.RewardManager
import com.ssverma.core.ads.quota.RewardPassStatus
import com.ssverma.core.ads.quota.RewardPassType
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.ListShareCardFormat
import com.ssverma.shared.domain.model.library.ListShareTheme
import com.ssverma.shared.domain.model.library.SecretSharedList
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.domain.repository.SecretSharedListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListShareExportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockSecretSharedListRepository: SecretSharedListRepository = mockk(relaxed = true)
    private lateinit var fakeBillingRepository: FakeBillingRepository
    private val mockRewardManager: RewardManager = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)
    private val passStatusFlow = MutableStateFlow(RewardPassStatus())

    private lateinit var viewModel: ListShareExportViewModel

    @Before
    fun setUp() {
        fakeBillingRepository = FakeBillingRepository(initialProActive = false)
        every { mockRewardManager.passStatus } returns passStatusFlow

        viewModel = ListShareExportViewModel(
            secretSharedListRepository = mockSecretSharedListRepository,
            billingRepository = fakeBillingRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager
        )
    }

    @Test
    fun `initial state has classic theme and locked luxury features when not pro or pass`() =
        runTest {
            val state = viewModel.uiState.value
            assertEquals(ListShareTheme.CLASSIC_SHOWTIME, state.selectedTheme)
            assertEquals(ListShareCardFormat.STORY_9_16, state.selectedFormat)
            assertFalse(state.isProActive)
            assertFalse(state.isPassActive)
            assertFalse(state.isWatermarkFree)
            assertFalse(state.isGateOpen)
        }

    @Test
    fun `select luxury theme opens gate when free user`() = runTest {
        viewModel.selectTheme(ListShareTheme.VINTAGE_35MM)

        val state = viewModel.uiState.value
        assertTrue(state.isGateOpen)
        assertEquals(ListShareTheme.VINTAGE_35MM, state.pendingTheme)
        assertEquals(ListShareTheme.CLASSIC_SHOWTIME, state.selectedTheme)
    }

    @Test
    fun `select classic theme does not open gate`() = runTest {
        viewModel.selectTheme(ListShareTheme.CLASSIC_SHOWTIME)

        val state = viewModel.uiState.value
        assertFalse(state.isGateOpen)
        assertNull(state.pendingTheme)
        assertEquals(ListShareTheme.CLASSIC_SHOWTIME, state.selectedTheme)
    }

    @Test
    fun `pro user selects luxury theme without gate and has watermark free export`() = runTest {
        fakeBillingRepository.setProActive(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isProActive)
        assertTrue(viewModel.uiState.value.isWatermarkFree)

        viewModel.selectTheme(ListShareTheme.NEON_CYBERPUNK)

        val state = viewModel.uiState.value
        assertFalse(state.isGateOpen)
        assertEquals(ListShareTheme.NEON_CYBERPUNK, state.selectedTheme)
    }

    @Test
    fun `reward pass unlocks luxury theme and removes watermark`() = runTest {
        passStatusFlow.value = RewardPassStatus(isListShareThemesUnlocked = true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isPassActive)
        assertTrue(viewModel.uiState.value.isWatermarkFree)

        viewModel.selectTheme(ListShareTheme.OLED_MIDNIGHT)
        assertEquals(ListShareTheme.OLED_MIDNIGHT, viewModel.uiState.value.selectedTheme)
        assertFalse(viewModel.uiState.value.isGateOpen)
    }

    @Test
    fun `watching rewarded ad grants LIST_SHARE_THEMES and selects pending theme`() = runTest {
        viewModel.selectTheme(ListShareTheme.VINTAGE_35MM)
        assertTrue(viewModel.uiState.value.isGateOpen)

        val rewardSlot = slot<() -> Unit>()
        every {
            mockRewardedAdManager.showRewardedAdIfReady(any(), capture(rewardSlot))
        } answers {
            rewardSlot.captured.invoke()
        }

        val mockActivity: Activity = mockk()
        viewModel.unlockThemesWithRewardedAd(mockActivity)
        advanceUntilIdle()

        coVerify { mockRewardManager.grantRewardPass(RewardPassType.LIST_SHARE_THEMES) }
        val state = viewModel.uiState.value
        assertTrue(state.isPassActive)
        assertTrue(state.isWatermarkFree)
        assertFalse(state.isGateOpen)
        assertEquals(ListShareTheme.VINTAGE_35MM, state.selectedTheme)
    }

    @Test
    fun `generateSecretLink succeeds and updates shareCode`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SECRET123",
            title = "Sci-Fi Gems",
            description = "Top sci-fi movies",
            ownerUserId = "user-1",
            ownerName = "John",
            isCollaborative = true,
            createdAtEpochMs = 123456789L,
            items = listOf(
                SecretSharedListItem(
                    mediaId = 1,
                    mediaType = MediaType.Movie,
                    title = "Interstellar",
                    posterImageUrl = "/path.jpg",
                    voteAvg = 8.6f
                )
            )
        )

        coEvery {
            mockSecretSharedListRepository.createSecretShare(
                title = any(),
                description = any(),
                items = any(),
                isCollaborative = any(),
                ownerName = any()
            )
        } returns Result.Success(sampleList)

        var returnedCode: String? = null
        viewModel.generateSecretLink(
            title = "Sci-Fi Gems",
            description = "Top sci-fi movies",
            items = sampleList.items,
            ownerName = "John",
            onSuccess = { code -> returnedCode = code }
        )
        advanceUntilIdle()

        assertEquals("SECRET123", returnedCode)
        assertEquals("SECRET123", viewModel.uiState.value.shareCode)
        assertEquals(sampleList, viewModel.uiState.value.secretSharedList)
        assertFalse(viewModel.uiState.value.isCreatingLink)
    }

    @Test
    fun `revokeSecretShare clears shareCode`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "REVOKE123",
            title = "My List",
            ownerUserId = "u1",
            ownerName = "John"
        )
        coEvery {
            mockSecretSharedListRepository.createSecretShare(any(), any(), any(), any(), any())
        } returns Result.Success(sampleList)

        viewModel.generateSecretLink("My List", null, emptyList(), "John") {}
        advanceUntilIdle()
        assertEquals("REVOKE123", viewModel.uiState.value.shareCode)

        var revokedCalled = false
        viewModel.revokeSecretShare { revokedCalled = true }
        advanceUntilIdle()

        coVerify { mockSecretSharedListRepository.revokeSecretShare("REVOKE123") }
        assertTrue(revokedCalled)
        assertNull(viewModel.uiState.value.shareCode)
        assertNull(viewModel.uiState.value.secretSharedList)
    }
}
