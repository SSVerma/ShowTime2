package com.ssverma.feature.filter.ui.discovery

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
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

    private val watchRegionFlow = MutableStateFlow("US")
    private val streamingSubscriptionsFlow = MutableStateFlow(setOf(8, 9))

    private lateinit var viewModel: UniversalDiscoveryViewModel

    @Before
    fun setUp() {
        coEvery { mockAppConfigRepository.watchProviderRegion } returns watchRegionFlow
        coEvery { mockAppConfigRepository.userStreamingSubscriptions } returns streamingSubscriptionsFlow
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
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "initialMediaType" to "Movie",
                    "initialVibe" to "ALL"
                )
            )
        )
    }

    @Test
    fun `initial state has correct default values`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.filter.mediaType).isEqualTo(MediaType.Movie)
        assertThat(state.filter.vibePreset).isEqualTo(DiscoveryVibePreset.ALL)
        assertThat(state.filter.watchRegion).isEqualTo("US")
        assertThat(state.filter.selectedProviderIds).containsExactly(8, 9)
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
}
