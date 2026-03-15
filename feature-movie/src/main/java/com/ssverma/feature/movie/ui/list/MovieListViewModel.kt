package com.ssverma.feature.movie.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.domain.usecase.PaginatedMoviesUseCase
import com.ssverma.feature.movie.navigation.MovieListDestination
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.args.MovieListingAvailableTypes
import com.ssverma.feature.movie.navigation.convertor.asMovieListingConfigs
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.movie.asMoviePreview
import com.ssverma.shared.domain.repository.AppConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MoviePaginatedListUiState(
    val isGridView: Boolean = true,
    val titleRes: Int = R.string.movies,
    val title: String? = null,
    val listingType: Int = 0,
    val isFilterApplicable: Boolean = false,
    val isFilterApplied: Boolean = false,
    val discoverConfig: MovieDiscoverConfig? = null
)

@HiltViewModel
class MovieListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paginatedMoviesUseCase: PaginatedMoviesUseCase,
    val appConfigRepository: AppConfigRepository
) : ViewModel() {


    private val movieListingArgs = savedStateHandle.buildMovieListingArgs()
    private val movieListingConfig = movieListingArgs.asMovieListingConfigs()

    private val _uiState = MutableStateFlow(
        MoviePaginatedListUiState(
            titleRes = if (movieListingArgs.titleRes == 0) R.string.movies else movieListingArgs.titleRes,
            title = movieListingArgs.title,
            listingType = movieListingArgs.listingType,
            isFilterApplicable = movieListingConfig is MovieListingConfig.Filterable,
            discoverConfig = (movieListingConfig as? MovieListingConfig.Filterable)?.discoverConfig
        )
    )
    val uiState: StateFlow<MoviePaginatedListUiState> = _uiState.asStateFlow()

    private val appliedFilters = MutableStateFlow(
        (movieListingConfig as? MovieListingConfig.Filterable)?.discoverConfig
            ?: MovieDiscoverConfig.builder().build()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedMovies: Flow<PagingData<MoviePreview>> = appliedFilters.flatMapLatest { filterConfig ->
        val config = if (movieListingConfig is MovieListingConfig.Filterable) {
            movieListingConfig.withFilter(filterConfig)
        } else {
            movieListingConfig
        }
        paginatedMoviesUseCase(config)
    }.map { pagingData ->
        pagingData.map { movie -> movie.asMoviePreview() }
    }.cachedIn(viewModelScope)

    fun onFiltersApplied(discoverConfig: MovieDiscoverConfig) {
        appliedFilters.update { discoverConfig }
        val isApplied = (movieListingConfig as? MovieListingConfig.Filterable)?.let {
            it.discoverConfig != discoverConfig && !discoverConfig.isBare()
        } ?: false
        _uiState.update {
            it.copy(
                isFilterApplied = isApplied,
                discoverConfig = discoverConfig
            )
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

}

private fun SavedStateHandle.buildMovieListingArgs(): MovieListingArgs {
    return MovieListingArgs(
        listingType = get<Int>(MovieListDestination.ArgListingType)
            ?: MovieListingAvailableTypes.Default,
        titleRes = get<Int>(MovieListDestination.ArgTitleRes) ?: 0,
        title = get<String>(MovieListDestination.ArgTitle),
        genreId = get<Int>(MovieListDestination.ArgGenreId) ?: 0,
        keywordId = get<Int>(MovieListDestination.ArgKeywordId) ?: 0
    )
}
