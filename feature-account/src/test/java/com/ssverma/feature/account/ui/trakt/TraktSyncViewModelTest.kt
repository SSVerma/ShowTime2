package com.ssverma.feature.account.ui.trakt

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.ads.quota.RewardManager
import com.ssverma.core.ads.quota.RewardPassStatus
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.R
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.shared.domain.model.trakt.TraktSyncResult
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.domain.repository.TraktSyncRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TraktSyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeBillingRepository = FakeBillingRepository(initialProActive = true)
    private val fakeTraktSyncRepository = FakeTraktSyncRepository()
    private val mockTraktAuthManager: TraktAuthManager = mockk(relaxed = true)
    private val mockRewardManager: RewardManager = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)

    private val traktAuthFlow = MutableStateFlow<TraktAuthState>(TraktAuthState.Disconnected)
    private val passStatusFlow = MutableStateFlow(RewardPassStatus())

    private lateinit var viewModel: TraktSyncViewModel

    @Before
    fun setUp() {
        every { mockTraktAuthManager.authState } returns traktAuthFlow
        every { mockRewardManager.passStatus } returns passStatusFlow

        viewModel = TraktSyncViewModel(
            traktAuthManager = mockTraktAuthManager,
            traktSyncRepository = fakeTraktSyncRepository,
            billingRepository = fakeBillingRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager
        )
    }

    @Test
    fun `initial state reflects disconnected trakt status`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.traktAuthState).isEqualTo(TraktAuthState.Disconnected)
            assertThat(state.isTraktSyncing).isFalse()
            assertThat(state.isTraktConnectSheetVisible).isFalse()
            assertThat(state.isPaywallVisible).isFalse()
        }
    }

    @Test
    fun `openTraktConnect and closeTraktConnect toggle sheet visibility`() = runTest {
        viewModel.openTraktConnect()
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isTraktConnectSheetVisible).isTrue()
        }

        viewModel.closeTraktConnect()
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isTraktConnectSheetVisible).isFalse()
        }
    }

    @Test
    fun `syncTraktNow executes sync on TraktSyncRepository when connected`() = runTest {
        traktAuthFlow.value = TraktAuthState.Connected(
            user = mockk(relaxed = true),
            accessToken = "test_token"
        )
        testScheduler.advanceUntilIdle()

        fakeTraktSyncRepository.syncResult = Result.success(
            TraktSyncResult(
                itemsImportedToWatchlist = 5,
                itemsImportedToHistory = 10,
                itemsExportedToTrakt = 2
            )
        )

        viewModel.syncTraktNow()
        testScheduler.advanceUntilIdle()

        assertThat(fakeTraktSyncRepository.syncLibraryCalledWithToken).isEqualTo("test_token")
        val state = viewModel.uiState.value
        assertThat(state.isTraktSyncing).isFalse()
        assertThat(state.message).isInstanceOf(UiText.StaticText::class.java)
        val staticText = state.message as UiText.StaticText
        assertThat(staticText.resId).isEqualTo(R.string.trakt_sync_success)
    }

    @Test
    fun `disconnectTrakt delegates to TraktAuthManager`() = runTest {
        viewModel.disconnectTrakt()

        coVerify { mockTraktAuthManager.disconnect() }
    }

    @Test
    fun `onConnectTraktClicked opens connect sheet directly when user is pro`() = runTest {
        fakeBillingRepository.setProActive(true)
        testScheduler.advanceUntilIdle()

        viewModel.onConnectTraktClicked()
        val state = viewModel.uiState.value
        assertThat(state.isTraktConnectSheetVisible).isTrue()
        assertThat(state.isQuotaGateVisible).isFalse()
    }

    @Test
    fun `onConnectTraktClicked shows quota gate when user is free without active pass`() = runTest {
        fakeBillingRepository.setProActive(false)
        passStatusFlow.value = RewardPassStatus(isTraktSyncUnlocked = false)
        testScheduler.advanceUntilIdle()

        viewModel.onConnectTraktClicked()
        val state = viewModel.uiState.value
        assertThat(state.isQuotaGateVisible).isTrue()
        assertThat(state.isTraktConnectSheetVisible).isFalse()
    }

    private class FakeTraktSyncRepository : TraktSyncRepository {
        var syncResult: Result<TraktSyncResult> = Result.success(
            TraktSyncResult(
                itemsImportedToWatchlist = 0,
                itemsImportedToHistory = 0,
                itemsExportedToTrakt = 0
            )
        )
        var syncLibraryCalledWithToken: String? = null

        override suspend fun syncLibrary(accessToken: String): Result<TraktSyncResult> {
            syncLibraryCalledWithToken = accessToken
            return syncResult
        }

        override suspend fun getUpNextQueue(accessToken: String): Result<List<TraktUpNextEpisode>> =
            Result.success(emptyList())

        override fun getUpNextQueueFlow(accessToken: String): Flow<List<TraktUpNextEpisode>> =
            flowOf(emptyList())

        override fun getWatchedEpisodesFlow(showId: Int, seasonNumber: Int): Flow<Set<Int>> =
            flowOf(emptySet())

        override fun getWatchedSeasonsFlow(showId: Int): Flow<Set<Int>> = flowOf(emptySet())
        override fun getSeasonWatchCountsFlow(showId: Int): Flow<Map<Int, Int>> = flowOf(emptyMap())
        override fun isEpisodeWatchedFlow(
            showId: Int,
            seasonNumber: Int,
            episodeNumber: Int
        ): Flow<Boolean> = flowOf(false)

        override suspend fun markEpisodeWatched(
            accessToken: String?,
            showTmdbId: Int,
            season: Int,
            episode: Int,
            showTitle: String,
            showPosterPath: String?,
            episodeTitle: String?,
            totalAired: Int
        ): Result<Unit> = Result.success(Unit)

        override suspend fun markSeasonWatched(
            accessToken: String?,
            showTmdbId: Int,
            season: Int,
            episodeNumbers: List<Int>,
            showTitle: String,
            showPosterPath: String?,
            totalAired: Int
        ): Result<Unit> = Result.success(Unit)

        override suspend fun markMovieWatched(
            accessToken: String?,
            movieTmdbId: Int
        ): Result<Unit> = Result.success(Unit)
    }
}
