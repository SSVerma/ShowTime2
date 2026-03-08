package com.ssverma.feature.movie.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ssverma.feature.filter.ui.filter.FilterUiState
import com.ssverma.feature.filter.ui.filter.asUiFilters
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.domain.usecase.MovieFilterUseCase
import com.ssverma.feature.movie.domain.usecase.PaginatedMoviesUseCase
import com.ssverma.feature.movie.navigation.MovieListDestination
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.args.MovieListingAvailableTypes
import com.ssverma.feature.movie.navigation.convertor.asMovieListingConfigs
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.movie.asMoviePreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoviePaginatedListUiState(
    val isGridView: Boolean = true,
    val filterUiState: FilterUiState = FilterUiState(filters = emptyList()),
    val titleRes: Int = R.string.movies,
    val title: String? = null,
    val listingType: Int = 0,
    val isFilterApplicable: Boolean = false
)

@HiltViewModel
class MovieListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paginatedMoviesUseCase: PaginatedMoviesUseCase,
    private val movieFilterUseCase: MovieFilterUseCase
) : ViewModel() {

    private val movieListingArgs = savedStateHandle.buildMovieListingArgs()
    private val movieListingConfig = movieListingArgs.asMovieListingConfigs()

    private val _uiState = MutableStateFlow(
        MoviePaginatedListUiState(
            titleRes = if (movieListingArgs.titleRes == 0) R.string.movies else movieListingArgs.titleRes,
            title = movieListingArgs.title,
            listingType = movieListingArgs.listingType,
            isFilterApplicable = movieListingConfig is MovieListingConfig.Filterable
        )
    )
    val uiState: StateFlow<MoviePaginatedListUiState> = _uiState.asStateFlow()

    private val appliedFilters = MutableStateFlow(MovieDiscoverConfig.builder().build())

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedMovies: Flow<PagingData<MoviePreview>> = appliedFilters
        .flatMapLatest { filterConfig ->
            if (movieListingConfig is MovieListingConfig.Filterable) {
                movieListingConfig.filterConfig = filterConfig
            }
            paginatedMoviesUseCase(movieListingConfig)
        }
        .map { pagingData ->
            pagingData.map { movie -> movie.asMoviePreview() }
        }
        .cachedIn(viewModelScope)

    init {
        if (_uiState.value.isFilterApplicable) {
            loadFilters()
        }
    }

    private fun loadFilters() {
        viewModelScope.launch {
            movieFilterUseCase().collect { result ->
                val filters = when (result) {
                    is Result.Success -> result.data.asUiFilters()
                    is Result.Error -> emptyList()
                }
                _uiState.update { it.copy(filterUiState = FilterUiState(filters = filters)) }
            }
        }
    }

    fun onFiltersApplied(discoverConfig: MovieDiscoverConfig) {
        appliedFilters.update { discoverConfig }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }
}

private fun SavedStateHandle.buildMovieListingArgs(): MovieListingArgs {
    return MovieListingArgs(
        listingType = get<Int>(MovieListDestination.ArgListingType) ?: MovieListingAvailableTypes.Default,
        titleRes = get<Int>(MovieListDestination.ArgTitleRes) ?: 0,
        title = get<String>(MovieListDestination.ArgTitle),
        genreId = get<Int>(MovieListDestination.ArgGenreId) ?: 0,
        keywordId = get<Int>(MovieListDestination.ArgKeywordId) ?: 0
    )
}
