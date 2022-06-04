package com.ssverma.feature.movie.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.feature.filter.ui.FilterUiState
import com.ssverma.feature.filter.ui.asUiFilters
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.domain.usecase.MovieFilterUseCase
import com.ssverma.feature.movie.domain.usecase.PaginatedMoviesUseCase
import com.ssverma.feature.movie.navigation.MovieListDestination
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.convertor.asMovieListingConfigs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paginatedMoviesUseCase: PaginatedMoviesUseCase,
    private val movieFilterUseCase: MovieFilterUseCase
) : ViewModel() {

    private val appliedFilters = MutableStateFlow(MovieDiscoverConfig.builder().build())

    // Put listing args as parcelable on Custom Nav Type
    private val movieListingArgs = savedStateHandle.buildMovieListingArgs()

    private val movieListingConfig = movieListingArgs.asMovieListingConfigs()

    val titleRes = if (movieListingArgs.titleRes == 0) {
        R.string.movies
    } else {
        movieListingArgs.titleRes
    }

    val title = movieListingArgs.title

    val listingType = movieListingArgs.listingType

    val filterApplicable =
        movieListingConfig is MovieListingConfig.Filterable

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedMovies: Flow<PagingData<Movie>> = appliedFilters.flatMapLatest { filterConfig ->
        if (movieListingConfig is MovieListingConfig.Filterable) {
            movieListingConfig.filterConfig = filterConfig
        }

        paginatedMoviesUseCase(movieListingConfig)

    }.cachedIn(viewModelScope)

    var filters by mutableStateOf(FilterUiState(filters = emptyList()))
        private set

    init {
        if (filterApplicable) {
            loadFilters()
        }
    }

    private fun loadFilters() {
        val discoverOptionResult = movieFilterUseCase()
        viewModelScope.launch {
            discoverOptionResult.collect { result ->
                filters = when (result) {
                    is Result.Error -> {
                        FilterUiState(filters = emptyList())
                    }
                    is Result.Success -> {
                        FilterUiState(
                            filters = result.data.asUiFilters()
                        )
                    }
                }
            }
        }
    }

    fun onFiltersApplied(discoverConfig: MovieDiscoverConfig) {
        this.appliedFilters.update { discoverConfig }
    }
}

private fun SavedStateHandle.buildMovieListingArgs(): MovieListingArgs {
    val genreId = get<Int>(MovieListDestination.ArgGenreId) ?: 0
    val keywordId = get<Int>(MovieListDestination.ArgKeywordId) ?: 0

    val listingType = get<Int>(MovieListDestination.ArgListingType) ?: 0
    val titleRes = get<Int>(MovieListDestination.ArgTitleRes) ?: 0
    val title = get<String>(MovieListDestination.ArgTitle)

    return MovieListingArgs(
        listingType = listingType,
        titleRes = titleRes,
        title = title,
        genreId = genreId,
        keywordId = keywordId
    )
}