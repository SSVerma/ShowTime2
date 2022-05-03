package com.ssverma.showtime.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.showtime.api.DiscoverQueryMap
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.repository.PersonRepository
import com.ssverma.showtime.data.repository.TvRepository
import com.ssverma.showtime.data.repository.UnsplashRepository
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.usecase.movie.*
import com.ssverma.showtime.ui.FetchDataUiState
import com.ssverma.showtime.ui.asFetchDataUiState
import com.ssverma.showtime.ui.movie.GenresUiState
import com.ssverma.showtime.ui.movie.MovieListUiState
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatAsIso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    tvRepository: TvRepository,
    unsplashRepository: UnsplashRepository,
    personRepository: PersonRepository,
    private val trendingMoviesUseCase: TrendingMoviesUseCase,
    private val topRatedMoviesUseCase: TopRatedMoviesUseCase,
    private val upcomingMoviesUseCase: UpcomingMoviesUseCase,
    private val inCinemaMoviesUseCase: InCinemaMoviesUseCase,
    private val popularMoviesUseCase: PopularMoviesUseCase,
    private val movieGeneUseCase: MovieGenresUseCase
) : ViewModel() {

    val movieBackdrop = unsplashRepository.randomMovieBackdropUrl

    val popularPersons: Flow<PagingData<Person>> = personRepository.fetchPopularPersonsGradually()
        .cachedIn(viewModelScope)

    val tvGenres = tvRepository.fetchTvGenre().asLiveData()

    val topRatedTvShows = tvRepository.fetchTopRatedTvShows().asLiveData()

    val dailyTrendingTvShows = tvRepository.fetchDailyTrendingTvShows().asLiveData()

    val todayAiringTvShows = tvRepository.discoverTvShows(
        queryMap = DiscoverQueryMap.ofTv(
            airDateLte = DateUtils.currentDate().formatAsIso(),
            airDateGte = DateUtils.currentDate().formatAsIso(),
        )
    ).asLiveData()

    val popularTvShows = tvRepository.discoverTvShows(
        queryMap = DiscoverQueryMap.ofMovie(
            sortBy = TmdbApiTiedConstants.AvailableSortingOptions.PopularityDesc
        )
    ).asLiveData()

    val nowAiringTvShows = tvRepository.discoverTvShows(
        queryMap = DiscoverQueryMap.ofTv(
            firstAirDateLte = DateUtils.currentDate().formatAsIso()
        )
    ).asLiveData()

    val upcomingTvShows = tvRepository.discoverTvShows(
        queryMap = DiscoverQueryMap.ofTv(
            firstAirDateGte = DateUtils.currentDate().plusDays(1).formatAsIso(),
            sortBy = TmdbApiTiedConstants.AvailableSortingOptions.FirstAirDateAsc
        )
    ).asLiveData()

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