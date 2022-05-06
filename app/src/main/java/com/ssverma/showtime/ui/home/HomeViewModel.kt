package com.ssverma.showtime.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.showtime.data.repository.UnsplashRepository
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.usecase.movie.*
import com.ssverma.showtime.domain.usecase.person.PopularPersonsPaginatedUseCase
import com.ssverma.showtime.domain.usecase.tv.*
import com.ssverma.showtime.ui.FetchDataUiState
import com.ssverma.showtime.ui.asFetchDataUiState
import com.ssverma.showtime.ui.movie.GenresUiState
import com.ssverma.showtime.ui.movie.MovieListUiState
import com.ssverma.showtime.ui.tv.TvShowListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    unsplashRepository: UnsplashRepository,
    private val trendingMoviesUseCase: TrendingMoviesUseCase,
    private val topRatedMoviesUseCase: TopRatedMoviesUseCase,
    private val upcomingMoviesUseCase: UpcomingMoviesUseCase,
    private val inCinemaMoviesUseCase: InCinemaMoviesUseCase,
    private val popularMoviesUseCase: PopularMoviesUseCase,
    private val movieGeneUseCase: MovieGenresUseCase,
    private val trendingTvShowsUseCase: TrendingTvShowsUseCase,
    private val topRatedTvShowsUseCase: TopRatedTvShowsUseCase,
    private val upcomingTvShowsUseCase: UpcomingTvShowsUseCase,
    private val todayAiringTvShowsUseCase: TodayAiringTvShowsUseCase,
    private val nowAiringTvShowsUseCase: NowAiringTvShowsUseCase,
    private val popularTvShowsUseCase: PopularTvShowsUseCase,
    private val tvGenresUseCase: TvGenresUseCase,
    popularPersonsUseCase: PopularPersonsPaginatedUseCase
) : ViewModel() {

    val movieBackdrop = unsplashRepository.randomMovieBackdropUrl

    val popularPersons: Flow<PagingData<Person>> = popularPersonsUseCase()
        .cachedIn(viewModelScope)

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

    var trendingTvShowsUiState by mutableStateOf<TvShowListUiState>(FetchDataUiState.Idle)
        private set

    var topRatedTvShowsUiState by mutableStateOf<TvShowListUiState>(FetchDataUiState.Idle)
        private set

    var popularTvShowsUiState by mutableStateOf<TvShowListUiState>(FetchDataUiState.Idle)
        private set

    var todayAiringTvShowsUiState by mutableStateOf<TvShowListUiState>(FetchDataUiState.Idle)
        private set

    var nowAiringTvShowsUiState by mutableStateOf<TvShowListUiState>(FetchDataUiState.Idle)
        private set

    var upcomingTvShowsUiState by mutableStateOf<TvShowListUiState>(FetchDataUiState.Idle)
        private set

    var tvGenresUiState by mutableStateOf<GenresUiState>(FetchDataUiState.Idle)
        private set

    init {
        fetchMovieGeneres()
        fetchTrendingMovies()
        fetchTopRatedMovies()
        fetchPopularMovies()
        fetchInCinemaMovies()
        fetchUpcomingMovies()

        fetchTvGeneres()
        fetchTrendingTvShows()
        fetchTopRatedTvShows()
        fetchPopularTvShows()
        fetchTodayAiringTvShows()
        fetchNowAiringTvShows()
        fetchUpcomingTvShows()
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

    fun fetchTvGeneres(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            tvGenresUiState = FetchDataUiState.Loading
            tvGenresUiState = tvGenresUseCase().asFetchDataUiState()
        }
    }

    fun fetchTrendingTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            trendingTvShowsUiState = FetchDataUiState.Loading
            trendingTvShowsUiState = trendingTvShowsUseCase(TimeWindow.Daily).asFetchDataUiState()
        }
    }

    fun fetchTopRatedTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            topRatedTvShowsUiState = FetchDataUiState.Loading
            topRatedTvShowsUiState = topRatedTvShowsUseCase().asFetchDataUiState()
        }
    }

    fun fetchPopularTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            popularTvShowsUiState = FetchDataUiState.Loading
            popularTvShowsUiState = popularTvShowsUseCase().asFetchDataUiState()
        }
    }

    fun fetchTodayAiringTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            todayAiringTvShowsUiState = FetchDataUiState.Loading
            todayAiringTvShowsUiState = todayAiringTvShowsUseCase().asFetchDataUiState()
        }
    }

    fun fetchNowAiringTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            nowAiringTvShowsUiState = FetchDataUiState.Loading
            nowAiringTvShowsUiState = nowAiringTvShowsUseCase().asFetchDataUiState()
        }
    }

    fun fetchUpcomingTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            upcomingTvShowsUiState = FetchDataUiState.Loading
            upcomingTvShowsUiState = upcomingTvShowsUseCase().asFetchDataUiState()
        }
    }

}