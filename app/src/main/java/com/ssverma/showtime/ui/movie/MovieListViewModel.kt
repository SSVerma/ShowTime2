package com.ssverma.showtime.ui.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.showtime.api.DiscoverMovieQueryMap
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.repository.MovieRepository
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatAsIso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

val movieReleaseType = QueryMultiValue.orBuilder()
    .or(TmdbApiTiedConstants.AvailableReleaseTypes.Theatrical)
    .build()

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private var movieListingType = ""

    private val filters = MutableStateFlow(mapOf<String, String>())

    val pagedMovies: Flow<PagingData<Movie>> = filters.transform {
        val pagedMovies = when (movieListingType) {
            MovieListingType.Trending -> {
                movieRepository.fetchTrendingMoviesGradually()
            }
            MovieListingType.TopRated -> {
                movieRepository.fetchTopRatedMoviesGradually()
            }
            else -> {
                movieRepository.discoverMoviesGradually(filters.value)
            }
        }.cachedIn(viewModelScope)

        pagedMovies.collect {
            emit(it)
        }
    }

    fun onMovieTypeAvailable(type: String) {
        this.movieListingType = type
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
            else -> DiscoverMovieQueryMap.of()
        }

        filters.value = filterMap
    }

}