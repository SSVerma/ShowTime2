package com.ssverma.showtime.ui.movie

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.showtime.R
import com.ssverma.showtime.api.DiscoverMovieQueryMap
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.repository.MovieRepository
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatAsIso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

val movieReleaseType = QueryMultiValue.orBuilder()
    .or(TmdbApiTiedConstants.AvailableReleaseTypes.Theatrical)
    .build()

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val appliedFilters = MutableStateFlow(mapOf<String, String>())

    private val genreId = savedStateHandle.get<Int>(AppDestination.MovieList.ArgGenreId)

    val listingType =
        savedStateHandle.get<MovieListingType>(AppDestination.MovieList.ArgListingType)
            ?: throw IllegalStateException("Movie listing type not provided")

    val titleRes = savedStateHandle.get<Int>(AppDestination.MovieList.ArgTitleRes)
        ?: R.string.movies

    val title = savedStateHandle.get<String>(AppDestination.MovieList.ArgTitle)

    val filterApplicable = when (listingType) {
        MovieListingType.TrendingToday,
        MovieListingType.TopRated -> false
        else -> true
    }

    val pagedMovies: Flow<PagingData<Movie>> = appliedFilters.flatMapLatest {
        when (listingType) {
            MovieListingType.TrendingToday -> {
                movieRepository.fetchTrendingMoviesGradually()
            }
            MovieListingType.TopRated -> {
                movieRepository.fetchTopRatedMoviesGradually()
            }
            else -> {
                movieRepository.discoverMoviesGradually(appliedFilters.value)
            }
        }.cachedIn(viewModelScope)
    }

    val filters = movieRepository.loadMovieFilters()

    init {
        fetchMovies(listingType)
    }

    private fun fetchMovies(type: MovieListingType) {
        val filterMap = when (type) {
            MovieListingType.Popular -> {
                DiscoverMovieQueryMap.of(
                    sortBy = TmdbApiTiedConstants.AvailableSortingOptions.PopularityDesc,
                    releaseType = movieReleaseType
                )
            }
            MovieListingType.NowInCinemas -> {
                DiscoverMovieQueryMap.of(
                    primaryReleaseDateLte = DateUtils.currentDate().formatAsIso(),
                    releaseType = movieReleaseType
                )
            }
            MovieListingType.Upcoming -> {
                DiscoverMovieQueryMap.of(
                    primaryReleaseDateGte = DateUtils.currentDate().plusDays(1).formatAsIso(),
                    releaseType = movieReleaseType,
                    sortBy = TmdbApiTiedConstants.AvailableSortingOptions.PrimaryReleaseDateAsc
                )
            }

            MovieListingType.Genre -> {
                val genreId = genreId
                    ?: throw IllegalArgumentException("Provide genre id when listing type is $type")

                DiscoverMovieQueryMap.of(
                    genres = QueryMultiValue.orBuilder().or(genreId).build()
                )
            }

            else -> DiscoverMovieQueryMap.of()
        }

        appliedFilters.value = filterMap
    }

    fun onFiltersApplied(appliedFilters: Map<String, String>) {
        this.appliedFilters.value = appliedFilters
    }

}