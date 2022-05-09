package com.ssverma.showtime.ui.tv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.usecase.tv.*
import com.ssverma.showtime.ui.FetchDataUiState
import com.ssverma.showtime.ui.asFetchDataUiState
import com.ssverma.showtime.ui.movie.GenresUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeTvShowViewModel @Inject constructor(
    private val trendingTvShowsUseCase: TrendingTvShowsUseCase,
    private val topRatedTvShowsUseCase: TopRatedTvShowsUseCase,
    private val upcomingTvShowsUseCase: UpcomingTvShowsUseCase,
    private val todayAiringTvShowsUseCase: TodayAiringTvShowsUseCase,
    private val nowAiringTvShowsUseCase: NowAiringTvShowsUseCase,
    private val popularTvShowsUseCase: PopularTvShowsUseCase,
    private val tvGenresUseCase: TvGenresUseCase,
) : ViewModel() {

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
        fetchTvGeneres()
        fetchTrendingTvShows()
        fetchTopRatedTvShows()
        fetchPopularTvShows()
        fetchTodayAiringTvShows()
        fetchNowAiringTvShows()
        fetchUpcomingTvShows()
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