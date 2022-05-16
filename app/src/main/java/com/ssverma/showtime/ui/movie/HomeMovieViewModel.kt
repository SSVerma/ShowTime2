package com.ssverma.showtime.ui.movie

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.asErrorOrSuccessUiState
import com.ssverma.showtime.data.repository.UnsplashRepository
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.usecase.movie.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeMovieViewModel @Inject constructor(
    unsplashRepository: UnsplashRepository,
    private val trendingMoviesUseCase: TrendingMoviesUseCase,
    private val topRatedMoviesUseCase: TopRatedMoviesUseCase,
    private val upcomingMoviesUseCase: UpcomingMoviesUseCase,
    private val inCinemaMoviesUseCase: InCinemaMoviesUseCase,
    private val popularMoviesUseCase: PopularMoviesUseCase,
    private val movieGeneUseCase: MovieGenresUseCase,
) : ViewModel() {

    val movieBackdrop = unsplashRepository.randomMovieBackdropUrl

    var trendingMoviesUiState by mutableStateOf<MovieListUiState>(UiState.Idle)
        private set

    var topRatedMoviesUiState by mutableStateOf<MovieListUiState>(UiState.Idle)
        private set

    var popularMoviesUiState by mutableStateOf<MovieListUiState>(UiState.Idle)
        private set

    var inCinemasMoviesUiState by mutableStateOf<MovieListUiState>(UiState.Idle)
        private set

    var upcomingMoviesUiState by mutableStateOf<MovieListUiState>(UiState.Idle)
        private set

    var movieGenresUiState by mutableStateOf<GenresUiState>(UiState.Idle)
        private set

    init {
        fetchMovieGeneres()
        fetchTrendingMovies()
        fetchTopRatedMovies()
        fetchPopularMovies()
        fetchInCinemaMovies()
        fetchUpcomingMovies()
    }

    fun fetchMovieGeneres(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            movieGenresUiState = UiState.Loading
            movieGenresUiState = movieGeneUseCase().asErrorOrSuccessUiState()
        }
    }

    fun fetchTrendingMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            trendingMoviesUiState = UiState.Loading
            trendingMoviesUiState =
                trendingMoviesUseCase(TimeWindow.Daily).asErrorOrSuccessUiState()
        }
    }

    fun fetchTopRatedMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            topRatedMoviesUiState = UiState.Loading
            topRatedMoviesUiState = topRatedMoviesUseCase().asErrorOrSuccessUiState()
        }
    }

    fun fetchPopularMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            popularMoviesUiState = UiState.Loading
            popularMoviesUiState = popularMoviesUseCase().asErrorOrSuccessUiState()
        }
    }

    fun fetchInCinemaMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            inCinemasMoviesUiState = UiState.Loading
            inCinemasMoviesUiState = inCinemaMoviesUseCase().asErrorOrSuccessUiState()
        }
    }

    fun fetchUpcomingMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            upcomingMoviesUiState = UiState.Loading
            upcomingMoviesUiState = upcomingMoviesUseCase().asErrorOrSuccessUiState()
        }
    }
}