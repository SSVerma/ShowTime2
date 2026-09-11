package com.ssverma.feature.filter.ui.discovery

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.ads.quota.RewardPassStatus
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.discovery.DiscoveryDecade
import com.ssverma.shared.domain.model.discovery.DiscoverySortOrder
import com.ssverma.shared.domain.model.discovery.DiscoveryVibePreset
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.DiscoveryRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.repository.WatchProviderRepository
import com.ssverma.shared.domain.usecase.discovery.GetRouletteSurpriseUseCase
import com.ssverma.shared.domain.usecase.discovery.GetUniversalDiscoveryUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UniversalDiscoveryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockGetUniversalDiscoveryUseCase: GetUniversalDiscoveryUseCase = mockk()
    private val mockGetRouletteSurpriseUseCase: GetRouletteSurpriseUseCase = mockk()
    private val mockDiscoveryRepository: DiscoveryRepository = mockk(relaxed = true)
    private val mockWatchProviderRepository: WatchProviderRepository = mockk(relaxed = true)
    private val mockAppConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val mockLibraryRepository: LibraryRepository = mockk(relaxed = true)
    private val mockBillingRepository: BillingRepository = mockk(relaxed = true)
    private val mockRewardManager: RewardManager = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)

    private val watchRegionFlow = MutableStateFlow("US")
    private val streamingSubscriptionsFlow = MutableStateFlow(setOf(8, 9))
    private val preferredOriginalLanguageFlow = MutableStateFlow("")
    private val isProUserFlow = MutableStateFlow(false)
    private val isBillingEnabledFlow = MutableStateFlow(true)
    private val passStatusFlow = MutableStateFlow(RewardPassStatus(isMultiServiceUnlocked = false))

    private lateinit var viewModel: UniversalDiscoveryViewModel

    @Before
    fun setUp() {
        coEvery { mockBillingRepository.isProActive } returns isProUserFlow
        coEvery { mockBillingRepository.isBillingEnabled } returns isBillingEnabledFlow
        coEvery { mockRewardManager.passStatus } returns passStatusFlow
        coEvery { mockAppConfigRepository.watchProviderRegion } returns watchRegionFlow
        coEvery { mockAppConfigRepository.userStreamingSubscriptions } returns streamingSubscriptionsFlow
        coEvery { mockAppConfigRepository.preferredOriginalLanguage } returns preferredOriginalLanguageFlow
        coEvery {
            mockGetUniversalDiscoveryUseCase(
                any(),
                any()
            )
        } returns Result.Success(emptyList())

        viewModel = UniversalDiscoveryViewModel(
            getUniversalDiscoveryUseCase = mockGetUniversalDiscoveryUseCase,
            getRouletteSurpriseUseCase = mockGetRouletteSurpriseUseCase,
            discoveryRepository = mockDiscoveryRepository,
            watchProviderRepository = mockWatchProviderRepository,
            appConfigRepository = mockAppConfigRepository,
            libraryRepository = mockLibraryRepository,
            billingRepository = mockBillingRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "initialMediaType" to "Movie",
                    "initialVibe" to "ALL"
                )
            )
        )
    }

    @Test
    fun `initial state has correct default values for free user`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.filter.mediaType).isEqualTo(MediaType.Movie)
        assertThat(state.filter.vibePreset).isEqualTo(DiscoveryVibePreset.ALL)
        assertThat(state.filter.watchRegion).isEqualTo("US")
        // Free user with multiple subscriptions only has first service active
        assertThat(state.filter.selectedProviderIds).containsExactly(8)
    }

    @Test
    fun `setMediaType updates media type and triggers search`() = runTest {
        advanceUntilIdle()
        viewModel.setMediaType(MediaType.Tv)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.filter.mediaType).isEqualTo(MediaType.Tv)
    }

    @Test
    fun `setVibePreset updates active vibe`() = runTest {
        advanceUntilIdle()
        viewModel.setVibePreset(DiscoveryVibePreset.MIND_BENDING)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.filter.vibePreset).isEqualTo(DiscoveryVibePreset.MIND_BENDING)
    }

    @Test
    fun `initial state parses continuous navigation arguments`() = runTest {
        val customVm = UniversalDiscoveryViewModel(
            getUniversalDiscoveryUseCase = mockGetUniversalDiscoveryUseCase,
            getRouletteSurpriseUseCase = mockGetRouletteSurpriseUseCase,
            discoveryRepository = mockDiscoveryRepository,
            watchProviderRepository = mockWatchProviderRepository,
            appConfigRepository = mockAppConfigRepository,
            libraryRepository = mockLibraryRepository,
            billingRepository = mockBillingRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "initialMediaType" to "Tv",
                    "initialVibe" to "COMFORT_BINGE",
                    "initialGenreId" to 35,
                    "initialProviderId" to 119,
                    "initialDecade" to "NINETIES_1990S",
                    "initialSortOrder" to "VOTE_AVERAGE_DESC"
                )
            )
        )
        advanceUntilIdle()

        val state = customVm.uiState.value
        assertThat(state.filter.mediaType).isEqualTo(MediaType.Tv)
        assertThat(state.filter.vibePreset).isEqualTo(DiscoveryVibePreset.COMFORT_BINGE)
        assertThat(state.filter.selectedGenreIds).containsExactly(35)
        assertThat(state.filter.selectedProviderIds).containsExactly(119)
        assertThat(state.filter.decade).isEqualTo(DiscoveryDecade.NINETIES_1990S)
        assertThat(state.filter.sortOrder).isEqualTo(DiscoverySortOrder.VOTE_AVERAGE_DESC)
    }

    @Test
    fun `applyFilter updates filter and dismisses sheet`() = runTest {
        advanceUntilIdle()
        val updated = viewModel.uiState.value.filter.copy(
            vibePreset = DiscoveryVibePreset.EPIC_WORLDS,
            decade = DiscoveryDecade.EIGHTIES_1980S
        )
        viewModel.openFilterSheet(true)
        assertThat(viewModel.uiState.value.isFilterSheetOpen).isTrue()

        viewModel.applyFilter(updated)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.filter.vibePreset).isEqualTo(DiscoveryVibePreset.EPIC_WORLDS)
        assertThat(viewModel.uiState.value.filter.decade).isEqualTo(DiscoveryDecade.EIGHTIES_1980S)
        assertThat(viewModel.uiState.value.isFilterSheetOpen).isFalse()
    }

    @Test
    fun `resetFilters resets filters to defaults`() = runTest {
        advanceUntilIdle()
        viewModel.setVibePreset(DiscoveryVibePreset.DARK_AND_GRITTY)
        viewModel.setDecade(DiscoveryDecade.GOLDEN_AGE)

        viewModel.resetFilters()
        advanceUntilIdle()

        val filter = viewModel.uiState.value.filter
        assertThat(filter.vibePreset).isEqualTo(DiscoveryVibePreset.ALL)
        assertThat(filter.decade).isEqualTo(DiscoveryDecade.ALL_TIME)
        assertThat(filter.sortOrder).isEqualTo(DiscoverySortOrder.POPULARITY_DESC)
        assertThat(filter.selectedProviderIds).isEmpty()
    }

    @Test
    fun `toggleViewMode switches between grid and list`() = runTest {
        assertThat(viewModel.uiState.value.isGridView).isTrue()
        viewModel.toggleViewMode()
        assertThat(viewModel.uiState.value.isGridView).isFalse()
    }

    @Test
    fun `spinRoulette sets spinning state and updates rouletteItem`() = runTest {
        val surpriseItem = UniversalMediaItem(
            id = 550,
            mediaType = MediaType.Movie,
            title = "Fight Club",
            overview = "Soap",
            posterImageUrl = "/fc.jpg",
            backdropImageUrl = "/fc_b.jpg",
            voteAvg = 8.4f,
            voteCount = 28000,
            releaseDate = "1999-10-15"
        )
        coEvery { mockGetRouletteSurpriseUseCase(any()) } returns Result.Success(surpriseItem)

        viewModel.spinRoulette()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isRouletteSpinning).isFalse()
        assertThat(state.rouletteItem).isEqualTo(surpriseItem)
    }

    @Test
    fun `free user selecting second provider triggers multi-service gate`() = runTest {
        coEvery { mockDiscoveryRepository.fetchWatchProviders(any()) } returns Result.Success(
            listOf(
                ProviderInfo(
                    providerId = 8,
                    providerName = "Netflix",
                    logoPath = "/netflix.png",
                    displayPriority = 1
                ),
                ProviderInfo(
                    providerId = 119,
                    providerName = "Amazon Prime",
                    logoPath = "/prime.png",
                    displayPriority = 2
                )
            )
        )
        advanceUntilIdle()

        // 8 is already selected for free user
        assertThat(viewModel.uiState.value.filter.selectedProviderIds).containsExactly(8)

        // Attempt to select second provider 119
        viewModel.toggleStreamingProvider(119)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isMultiServiceGateOpen).isTrue()
        assertThat(state.filter.selectedProviderIds).containsExactly(8)
    }

    @Test
    fun `switchToProvider replaces single provider and dismisses gate`() = runTest {
        advanceUntilIdle()
        viewModel.toggleStreamingProvider(119)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isMultiServiceGateOpen).isTrue()

        viewModel.switchToProvider(119)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isMultiServiceGateOpen).isFalse()
        assertThat(state.pendingProviderToSwitch).isNull()
        assertThat(state.filter.selectedProviderIds).containsExactly(119)
    }

    @Test
    fun `pro user can select multiple providers without gate`() = runTest {
        isProUserFlow.value = true
        advanceUntilIdle()

        val proVm = UniversalDiscoveryViewModel(
            getUniversalDiscoveryUseCase = mockGetUniversalDiscoveryUseCase,
            getRouletteSurpriseUseCase = mockGetRouletteSurpriseUseCase,
            discoveryRepository = mockDiscoveryRepository,
            watchProviderRepository = mockWatchProviderRepository,
            appConfigRepository = mockAppConfigRepository,
            libraryRepository = mockLibraryRepository,
            billingRepository = mockBillingRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        // Pro user gets all configured subscriptions by default
        assertThat(proVm.uiState.value.filter.selectedProviderIds).containsExactly(8, 9)

        // Select 3rd provider 337
        proVm.toggleStreamingProvider(337)
        advanceUntilIdle()

        val state = proVm.uiState.value
        assertThat(state.isMultiServiceGateOpen).isFalse()
        assertThat(state.filter.selectedProviderIds).containsExactly(8, 9, 337)
    }

    @Test
    fun `toggleMyServicesFilter toggles subscriptions on and off`() = runTest {
        advanceUntilIdle()
        // Reset providers
        viewModel.toggleStreamingProvider(8)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.filter.selectedProviderIds).isEmpty()

        // Toggle My Services on (free user gets 1st service)
        viewModel.toggleMyServicesFilter()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.filter.selectedProviderIds).containsExactly(8)

        // Toggle My Services off
        viewModel.toggleMyServicesFilter()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.filter.selectedProviderIds).isEmpty()
    }

    @Test
    fun `updating streaming subscriptions while My Services is active automatically updates filter`() =
        runTest {
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.filter.selectedProviderIds).containsExactly(8)

            streamingSubscriptionsFlow.value = setOf(119, 337)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.filter.selectedProviderIds).containsExactly(119)
        }

    @Test
    fun `clearing streaming subscriptions while My Services is active clears filter`() = runTest {
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.filter.selectedProviderIds).containsExactly(8)

        streamingSubscriptionsFlow.value = emptySet()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.filter.selectedProviderIds).isEmpty()
    }
}
