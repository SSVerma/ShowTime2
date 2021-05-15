package com.ssverma.showtime.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ssverma.showtime.api.DiscoverMovieQueryMap
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.repository.MovieRepository
import com.ssverma.showtime.data.repository.UnsplashRepository
import com.ssverma.showtime.ui.movie.movieReleaseType
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatAsIso
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    movieRepository: MovieRepository,
    unsplashRepository: UnsplashRepository
) : ViewModel() {

    val movieBackdrop = unsplashRepository.randomMovieBackdropUrl

//    val mockMovies = movieRepository.fetchMockMovies().asLiveData()

    val topRatedMovies = movieRepository.fetchTopRatedMovies().asLiveData()
    val dailyTrendingMovies = movieRepository.fetchDailyTrendingMovies().asLiveData()

    val genres = movieRepository.fetchMovieGenre().asLiveData()

    val popularMovies = movieRepository.discoverMovies(
        queryMap = DiscoverMovieQueryMap.of(
            sortBy = TmdbApiTiedConstants.AvailableSortingOptions.PopularityDesc,
            releaseType = movieReleaseType,
        )
    ).asLiveData()

    val nowInCinemasMovies = movieRepository.discoverMovies(
        queryMap = DiscoverMovieQueryMap.of(
            primaryReleaseDateLte = DateUtils.currentDate().formatAsIso(),
            releaseType = movieReleaseType,
        )
    ).asLiveData()

    val upcomingMovies = movieRepository.discoverMovies(
        queryMap = DiscoverMovieQueryMap.of(
            primaryReleaseDateGte = DateUtils.currentDate().plusDays(1).formatAsIso(),
            releaseType = movieReleaseType,
            sortBy = TmdbApiTiedConstants.AvailableSortingOptions.PrimaryReleaseDateAsc
        )
    ).asLiveData()
}