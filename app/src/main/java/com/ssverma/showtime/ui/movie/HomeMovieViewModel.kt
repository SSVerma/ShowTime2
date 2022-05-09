package com.ssverma.showtime.ui.movie

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.showtime.data.repository.UnsplashRepository
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.usecase.movie.*
import com.ssverma.showtime.ui.FetchDataUiState
import com.ssverma.showtime.ui.asFetchDataUiState
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

    var trendingMoviesUiState by mutableStateOf<MovieListUiState>(FetchDataUiState.Idle)
        private set

    var topRatedMoviesUiState by mutableStateOf<MovieListUiState>(FetchDataUiState.Idle)
        private set

    var popularMoviesUiState by mutableStateOf<MovieListUiState>(FetchDataUiState.Idle)
        private set

    var inCinemasMoviesUiState by mutableStateOf<MovieListUiState>(FetchDataUiState.Idle)
        private set

    var upcomingMoviesUiState by mutableStateOf<MovieListUiState>(FetchDataUiState.Idle)
        private set

    var movieGenresUiState by mutableStateOf<GenresUiState>(FetchDataUiState.Idle)
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
            movieGenresUiState = FetchDataUiState.Loading
            movieGenresUiState = movieGeneUseCase().asFetchDataUiState()
        }
    }

    fun fetchTrendingMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            trendingMoviesUiState = FetchDataUiState.Loading
            trendingMoviesUiState = trendingMoviesUseCase(TimeWindow.Daily).asFetchDataUiState()
        }
    }

    fun fetchTopRatedMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            topRatedMoviesUiState = FetchDataUiState.Loading
            topRatedMoviesUiState = topRatedMoviesUseCase().asFetchDataUiState()
        }
    }

    fun fetchPopularMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            popularMoviesUiState = FetchDataUiState.Loading
            popularMoviesUiState = popularMoviesUseCase().asFetchDataUiState()
        }
    }

    fun fetchInCinemaMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            inCinemasMoviesUiState = FetchDataUiState.Loading
            inCinemasMoviesUiState = inCinemaMoviesUseCase().asFetchDataUiState()
        }
    }

    fun fetchUpcomingMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            upcomingMoviesUiState = FetchDataUiState.Loading
            upcomingMoviesUiState = upcomingMoviesUseCase().asFetchDataUiState()
        }
    }
}