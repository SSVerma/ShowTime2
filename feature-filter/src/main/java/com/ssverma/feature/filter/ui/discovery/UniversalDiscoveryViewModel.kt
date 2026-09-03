package com.ssverma.feature.filter.ui.discovery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.discovery.DiscoveryDecade
import com.ssverma.shared.domain.model.discovery.DiscoverySortOrder
import com.ssverma.shared.domain.model.discovery.DiscoveryStudioHub
import com.ssverma.shared.domain.model.discovery.DiscoveryVibePreset
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.DiscoveryRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.repository.WatchProviderRepository
import com.ssverma.shared.domain.usecase.discovery.GetRouletteSurpriseUseCase
import com.ssverma.shared.domain.usecase.discovery.GetUniversalDiscoveryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UniversalDiscoveryViewModel @Inject constructor(
    private val getUniversalDiscoveryUseCase: GetUniversalDiscoveryUseCase,
    private val getRouletteSurpriseUseCase: GetRouletteSurpriseUseCase,
    private val discoveryRepository: DiscoveryRepository,
    private val watchProviderRepository: WatchProviderRepository,
    private val appConfigRepository: AppConfigRepository,
    private val libraryRepository: LibraryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(UniversalDiscoveryUiState())
    val uiState: StateFlow<UniversalDiscoveryUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<String>()
    val uiEffect: SharedFlow<String> = _uiEffect.asSharedFlow()

    private var searchJob: Job? = null
    private var currentPage = 1

    init {
        val initialMediaTypeStr = savedStateHandle.get<String>("initialMediaType")
        val initialMediaType = if (initialMediaTypeStr == "Tv") MediaType.Tv else MediaType.Movie

        val initialVibeStr = savedStateHandle.get<String>("initialVibe")
        val initialVibe = try {
            initialVibeStr?.let { DiscoveryVibePreset.valueOf(it) } ?: DiscoveryVibePreset.ALL
        } catch (_: Exception) {
            DiscoveryVibePreset.ALL
        }

        val initialStudioStr = savedStateHandle.get<String>("initialStudioHub")
        val initialStudio = try {
            initialStudioStr?.let { DiscoveryStudioHub.valueOf(it) }
        } catch (_: Exception) {
            null
        }

        val initialGenreId = savedStateHandle.get<Int>("initialGenreId")
        val initialGenres =
            if (initialGenreId != null && initialGenreId > 0) setOf(initialGenreId) else emptySet()

        val initialProviderId = savedStateHandle.get<Int>("initialProviderId")
        val initialProviders =
            if (initialProviderId != null && initialProviderId > 0) setOf(initialProviderId) else emptySet()

        val initialDecadeStr = savedStateHandle.get<String>("initialDecade")
        val initialDecade = try {
            initialDecadeStr?.let { DiscoveryDecade.valueOf(it) } ?: DiscoveryDecade.ALL_TIME
        } catch (_: Exception) {
            DiscoveryDecade.ALL_TIME
        }

        val initialSortStr = savedStateHandle.get<String>("initialSortOrder")
        val initialSort = try {
            initialSortStr?.let { DiscoverySortOrder.valueOf(it) }
                ?: DiscoverySortOrder.POPULARITY_DESC
        } catch (_: Exception) {
            DiscoverySortOrder.POPULARITY_DESC
        }

        _uiState.update {
            it.copy(
                filter = it.filter.copy(
                    mediaType = initialMediaType,
                    vibePreset = initialVibe,
                    studioHub = initialStudio,
                    selectedGenreIds = initialGenres,
                    selectedProviderIds = initialProviders,
                    decade = initialDecade,
                    sortOrder = initialSort
                )
            )
        }

        viewModelScope.launch {
            val regionsResult = watchProviderRepository.fetchAvailableWatchRegions()
            if (regionsResult is Result.Success) {
                _uiState.update { it.copy(availableRegions = regionsResult.data) }
            }
        }

        viewModelScope.launch {
            appConfigRepository.watchProviderRegion.collectLatest { region ->
                _uiState.update {
                    it.copy(filter = it.filter.copy(watchRegion = region))
                }
                loadAvailableProviders()
                scheduleQuery(debounceMs = 0)
            }
        }

        viewModelScope.launch {
            appConfigRepository.userStreamingSubscriptions.collectLatest { subscriptions ->
                if (initialProviders.isEmpty()) {
                    _uiState.update {
                        it.copy(filter = it.filter.copy(selectedProviderIds = subscriptions))
                    }
                    scheduleQuery(debounceMs = 150)
                }
            }
        }
    }

    private fun loadAvailableProviders() {
        viewModelScope.launch {
            val isMovie = _uiState.value.filter.mediaType == MediaType.Movie
            val result = discoveryRepository.fetchWatchProviders(isMovie)
            if (result is Result.Success) {
                _uiState.update { it.copy(availableProviders = result.data.take(15)) }
            }
        }
    }

    private fun scheduleQuery(debounceMs: Long = 300) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounceMs > 0) {
                delay(debounceMs)
            }
            currentPage = 1
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    hasReachedEnd = false
                )
            }

            val filter = _uiState.value.filter
            val result = getUniversalDiscoveryUseCase(filter, page = 1)

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            items = result.data,
                            isLoading = false,
                            hasReachedEnd = result.data.isEmpty()
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error.toString()
                        )
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.hasReachedEnd) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = currentPage + 1
            val result = getUniversalDiscoveryUseCase(_uiState.value.filter, page = nextPage)

            when (result) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        _uiState.update {
                            it.copy(isLoadingMore = false, hasReachedEnd = true)
                        }
                    } else {
                        currentPage = nextPage
                        _uiState.update {
                            it.copy(
                                items = it.items + result.data,
                                isLoadingMore = false
                            )
                        }
                    }
                }

                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            }
        }
    }

    fun setMediaType(mediaType: MediaType) {
        if (_uiState.value.filter.mediaType == mediaType) return
        _uiState.update {
            it.copy(filter = it.filter.copy(mediaType = mediaType))
        }
        loadAvailableProviders()
        scheduleQuery(debounceMs = 0)
    }

    fun setVibePreset(vibePreset: DiscoveryVibePreset) {
        _uiState.update {
            it.copy(filter = it.filter.copy(vibePreset = vibePreset))
        }
        scheduleQuery()
    }

    fun applyFilter(newFilter: UniversalDiscoveryFilter) {
        _uiState.update {
            it.copy(
                filter = newFilter,
                isFilterSheetOpen = false
            )
        }
        scheduleQuery(debounceMs = 0)
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                filter = it.filter.copy(
                    vibePreset = DiscoveryVibePreset.ALL,
                    decade = DiscoveryDecade.ALL_TIME,
                    sortOrder = DiscoverySortOrder.POPULARITY_DESC,
                    studioHub = null,
                    selectedGenreIds = emptySet(),
                    minRating = null,
                    hideWatched = true
                )
            )
        }
        scheduleQuery(debounceMs = 0)
    }

    fun setDecade(decade: DiscoveryDecade) {
        _uiState.update {
            it.copy(filter = it.filter.copy(decade = decade))
        }
        scheduleQuery()
    }

    fun setSortOrder(sortOrder: DiscoverySortOrder) {
        _uiState.update {
            it.copy(filter = it.filter.copy(sortOrder = sortOrder))
        }
        scheduleQuery()
    }

    fun setStudioHub(studioHub: DiscoveryStudioHub?) {
        _uiState.update {
            it.copy(filter = it.filter.copy(studioHub = studioHub))
        }
        scheduleQuery()
    }

    fun toggleStreamingProvider(providerId: Int) {
        val current = _uiState.value.filter.selectedProviderIds
        val updated = if (current.contains(providerId)) {
            current - providerId
        } else {
            current + providerId
        }
        viewModelScope.launch {
            appConfigRepository.updateStreamingSubscriptions(updated)
        }
    }

    fun toggleHideWatched(hideWatched: Boolean) {
        _uiState.update {
            it.copy(filter = it.filter.copy(hideWatched = hideWatched))
        }
        scheduleQuery(debounceMs = 0)
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun openFilterSheet(open: Boolean) {
        _uiState.update { it.copy(isFilterSheetOpen = open) }
    }

    fun openRegionSheet(open: Boolean) {
        _uiState.update { it.copy(isRegionSheetOpen = open) }
    }

    fun spinRoulette() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRouletteSpinning = true, rouletteItem = null) }
            delay(1200) // Cinematic spin delay
            val result = getRouletteSurpriseUseCase(_uiState.value.filter)
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isRouletteSpinning = false, rouletteItem = result.data)
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(isRouletteSpinning = false, rouletteItem = null)
                    }
                }
            }
        }
    }

    fun dismissRoulette() {
        _uiState.update { it.copy(rouletteItem = null, isRouletteSpinning = false) }
    }

    fun updateRegion(regionCode: String) {
        viewModelScope.launch {
            appConfigRepository.updateWatchProviderRegion(regionCode)
            _uiState.update { it.copy(isRegionSheetOpen = false) }
        }
    }

    fun toggleFavorite(item: UniversalMediaItem) {
        viewModelScope.launch {
            val isFav = libraryRepository.toggleFavorite(
                mediaId = item.id,
                mediaType = item.mediaType,
                title = item.title,
                posterImageUrl = item.posterImageUrl,
                backdropImageUrl = item.backdropImageUrl,
                voteAvg = item.voteAvg,
                releaseDate = item.releaseDate
            )
            updateItemInState(item.id) { it.copy(isFavorite = isFav) }
            val message = if (isFav) "Added to Favorites" else "Removed from Favorites"
            _uiEffect.emit(message)
        }
    }

    fun toggleWatchlist(item: UniversalMediaItem) {
        viewModelScope.launch {
            val inWatchlist = libraryRepository.toggleWatchlist(
                mediaId = item.id,
                mediaType = item.mediaType,
                title = item.title,
                posterImageUrl = item.posterImageUrl,
                backdropImageUrl = item.backdropImageUrl,
                voteAvg = item.voteAvg,
                releaseDate = item.releaseDate
            )
            updateItemInState(item.id) { it.copy(isInWatchlist = inWatchlist) }
            val message = if (inWatchlist) "Added to Watchlist" else "Removed from Watchlist"
            _uiEffect.emit(message)
        }
    }

    fun toggleWatchHistory(item: UniversalMediaItem) {
        viewModelScope.launch {
            val watched = libraryRepository.toggleWatchHistory(
                mediaId = item.id,
                mediaType = item.mediaType,
                title = item.title,
                posterImageUrl = item.posterImageUrl,
                voteAvg = item.voteAvg
            )
            updateItemInState(item.id) { it.copy(isWatched = watched) }
            val message = if (watched) "Marked as Watched" else "Removed from Watched"
            _uiEffect.emit(message)
        }
    }

    private fun updateItemInState(id: Int, transform: (UniversalMediaItem) -> UniversalMediaItem) {
        _uiState.update { state ->
            val updatedItems = state.items.map { if (it.id == id) transform(it) else it }
            val updatedRoulette =
                if (state.rouletteItem?.id == id) state.rouletteItem.let(transform) else state.rouletteItem
            state.copy(items = updatedItems, rouletteItem = updatedRoulette)
        }
    }
}
