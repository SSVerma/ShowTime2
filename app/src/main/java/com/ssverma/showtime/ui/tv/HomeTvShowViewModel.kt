package com.ssverma.showtime.ui.tv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.usecase.tv.*
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.asErrorOrSuccessUiState
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

    var trendingTvShowsUiState by mutableStateOf<TvShowListUiState>(UiState.Idle)
        private set

    var topRatedTvShowsUiState by mutableStateOf<TvShowListUiState>(UiState.Idle)
        private set

    var popularTvShowsUiState by mutableStateOf<TvShowListUiState>(UiState.Idle)
        private set

    var todayAiringTvShowsUiState by mutableStateOf<TvShowListUiState>(UiState.Idle)
        private set

    var nowAiringTvShowsUiState by mutableStateOf<TvShowListUiState>(UiState.Idle)
        private set

    var upcomingTvShowsUiState by mutableStateOf<TvShowListUiState>(UiState.Idle)
        private set

    var tvGenresUiState by mutableStateOf<GenresUiState>(UiState.Idle)
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
            tvGenresUiState = UiState.Loading
            tvGenresUiState = tvGenresUseCase().asErrorOrSuccessUiState()
        }
    }

    fun fetchTrendingTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            trendingTvShowsUiState = UiState.Loading
            trendingTvShowsUiState = trendingTvShowsUseCase(TimeWindow.Daily).asErrorOrSuccessUiState()
        }
    }

    fun fetchTopRatedTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            topRatedTvShowsUiState = UiState.Loading
            topRatedTvShowsUiState = topRatedTvShowsUseCase().asErrorOrSuccessUiState()
        }
    }

    fun fetchPopularTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            popularTvShowsUiState = UiState.Loading
            popularTvShowsUiState = popularTvShowsUseCase().asErrorOrSuccessUiState()
        }
    }

    fun fetchTodayAiringTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            todayAiringTvShowsUiState = UiState.Loading
            todayAiringTvShowsUiState = todayAiringTvShowsUseCase().asErrorOrSuccessUiState()
        }
    }

    fun fetchNowAiringTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            nowAiringTvShowsUiState = UiState.Loading
            nowAiringTvShowsUiState = nowAiringTvShowsUseCase().asErrorOrSuccessUiState()
        }
    }

    fun fetchUpcomingTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            upcomingTvShowsUiState = UiState.Loading
            upcomingTvShowsUiState = upcomingTvShowsUseCase().asErrorOrSuccessUiState()
        }
    }
}