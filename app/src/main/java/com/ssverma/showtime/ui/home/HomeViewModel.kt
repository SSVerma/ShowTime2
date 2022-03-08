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
import com.ssverma.showtime.data.repository.MovieRepository
import com.ssverma.showtime.data.repository.PersonRepository
import com.ssverma.showtime.data.repository.TvRepository
import com.ssverma.showtime.data.repository.UnsplashRepository
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.movie.GetTrendingMoviesUseCase
import com.ssverma.showtime.ui.FetchDataUiState
import com.ssverma.showtime.ui.asFetchDataUiState
import com.ssverma.showtime.ui.movie.MovieListUiState
import com.ssverma.showtime.ui.movie.movieReleaseType
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatAsIso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    movieRepository: MovieRepository,
    tvRepository: TvRepository,
    unsplashRepository: UnsplashRepository,
    personRepository: PersonRepository,
    private val trendingMoviesUseCase: GetTrendingMoviesUseCase
) : ViewModel() {

    val movieBackdrop = unsplashRepository.randomMovieBackdropUrl
    val tvShowBackdrop = unsplashRepository.randomTvShowBackdropUrl

//    val mockMovies = movieRepository.fetchMockMovies().asLiveData()

    val topRatedMovies = movieRepository.fetchTopRatedMovies().asLiveData()
    val dailyTrendingMovies = movieRepository.fetchDailyTrendingMovies().asLiveData()

    val movieGenres = movieRepository.fetchMovieGenre().asLiveData()

    val popularMovies = movieRepository.discoverMovies(
        queryMap = DiscoverQueryMap.ofMovie(
            sortBy = TmdbApiTiedConstants.AvailableSortingOptions.PopularityDesc,
            releaseType = movieReleaseType,
        )
    ).asLiveData()

    val nowInCinemasMovies = movieRepository.discoverMovies(
        queryMap = DiscoverQueryMap.ofMovie(
            primaryReleaseDateLte = DateUtils.currentDate().formatAsIso(),
            releaseType = movieReleaseType,
        )
    ).asLiveData()

    val upcomingMovies = movieRepository.discoverMovies(
        queryMap = DiscoverQueryMap.ofMovie(
            primaryReleaseDateGte = DateUtils.currentDate().plusDays(1).formatAsIso(),
            releaseType = movieReleaseType,
            sortBy = TmdbApiTiedConstants.AvailableSortingOptions.PrimaryReleaseDateAsc
        )
    ).asLiveData()

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

    init {
        fetchTrendingMovies()
    }

    fun fetchTrendingMovies(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            trendingMoviesUiState = FetchDataUiState.Loading
            trendingMoviesUiState = trendingMoviesUseCase(TimeWindow.Daily).asFetchDataUiState()
        }
    }

}