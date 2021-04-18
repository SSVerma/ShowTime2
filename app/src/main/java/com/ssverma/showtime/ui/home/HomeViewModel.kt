package com.ssverma.showtime.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ssverma.showtime.api.DiscoverMovieQueryMap
import com.ssverma.showtime.data.MovieCategory
import com.ssverma.showtime.data.domain.ApiTiedConstants
import com.ssverma.showtime.data.repository.MovieRepository
import com.ssverma.showtime.data.repository.UnsplashRepository
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatAsIso
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val unsplashRepository: UnsplashRepository
) : ViewModel() {

    val movieBackdrop = unsplashRepository.randomMovieBackdropUrl

    val movieCategories = movieRepository.movieCategories

    val mockMovies = movieRepository.fetchMockMovies().asLiveData()

    val latestMovie = movieRepository.fetchLatestMovie().asLiveData()

    val popularMovies = movieRepository.fetchPopularMovies().asLiveData()

    val dailyTrendingMovies = movieRepository.fetchDailyTrendingMovies().asLiveData()

    val freeMovies = movieRepository.discoverMovies(
        queryMap = DiscoverMovieQueryMap.of(monetizationType = ApiTiedConstants.AvailableMonetizationTypes.Free)
    ).asLiveData()

    val justReleasedMovies = movieRepository.discoverMovies(
        queryMap = DiscoverMovieQueryMap.of(
            primaryReleaseDateLte = DateUtils.currentDate().formatAsIso()
        )
    ).asLiveData()

    fun onCategorySelected(movieCategory: MovieCategory) {
        //
    }
}