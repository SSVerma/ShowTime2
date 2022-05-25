package com.ssverma.showtime.ui.movie

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.core.domain.Result
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.MovieDiscoverConfig
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.model.movie.MovieListingConfig
import com.ssverma.showtime.domain.usecase.filter.MovieFilterUseCase
import com.ssverma.showtime.domain.usecase.movie.PaginatedMoviesUseCase
import com.ssverma.showtime.ui.filter.FilterUiState
import com.ssverma.showtime.ui.filter.asUiFilters
import com.ssverma.showtime.ui.movie.navigation.MovieListDestination
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

    val filterApplicable = movieListingConfig is MovieListingConfig.Filterable

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