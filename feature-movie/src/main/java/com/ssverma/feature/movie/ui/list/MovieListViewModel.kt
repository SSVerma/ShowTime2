package com.ssverma.feature.movie.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.domain.usecase.PaginatedMoviesUseCase
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.convertor.asMovieListingConfig
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.movie.asMoviePreview
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

data class MoviePaginatedListUiState(
    val isGridView: Boolean = true,
    val titleRes: Int = R.string.movies,
    val title: String? = null,
    val config: MovieListingConfig,
    val isFilterApplicable: Boolean = false,
    val isFilterApplied: Boolean = false,
    val filterConfig: MovieDiscoverConfig? = null,
)

@HiltViewModel(assistedFactory = MovieListViewModel.Factory::class)
class MovieListViewModel @AssistedInject constructor(
    @Assisted private val route: MovieListingArgs,
    private val paginatedMoviesUseCase: PaginatedMoviesUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(route: MovieListingArgs): MovieListViewModel
    }

    private val movieListingConfig = route.asMovieListingConfig()

    private val _uiState = MutableStateFlow(
        MoviePaginatedListUiState(
            titleRes = route.titleRes ?: R.string.movies,
            title = route.title,
            config = movieListingConfig,
            isFilterApplicable = movieListingConfig is MovieListingConfig.Filterable,
            // Pre-populate with the initial config from the route if it exists
            filterConfig = (movieListingConfig as? MovieListingConfig.Filterable)?.discoverConfig
        )
    )

    val uiState: StateFlow<MoviePaginatedListUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedMovies: Flow<PagingData<MoviePreview>> = _uiState
        .map { state ->
            if (movieListingConfig is MovieListingConfig.Filterable && state.filterConfig != null) {
                movieListingConfig.withFilter(state.filterConfig)
            } else {
                movieListingConfig
            }
        }
        .distinctUntilChanged()
        .flatMapLatest { config ->
            paginatedMoviesUseCase(config)
        }.map { pagingData ->
            pagingData.map { movie -> movie.asMoviePreview() }
        }.cachedIn(viewModelScope)

    fun onFiltersApplied(filterConfig: MovieDiscoverConfig) {
        val isApplied = (movieListingConfig as? MovieListingConfig.Filterable)?.let {
            it.discoverConfig != filterConfig && !filterConfig.isBare()
        } ?: false

        _uiState.update {
            it.copy(
                isFilterApplied = isApplied,
                filterConfig = filterConfig
            )
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }
}
