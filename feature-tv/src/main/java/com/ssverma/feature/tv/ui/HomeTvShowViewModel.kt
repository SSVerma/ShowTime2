package com.ssverma.feature.tv.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.domain.TimeWindow
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.asSuccessOrErrorUiState
import com.ssverma.feature.tv.domain.usecase.*
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
            tvGenresUiState = tvGenresUseCase().asSuccessOrErrorUiState()
        }
    }

    fun fetchTrendingTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            trendingTvShowsUiState = UiState.Loading
            trendingTvShowsUiState = trendingTvShowsUseCase(TimeWindow.Daily).asSuccessOrErrorUiState()
        }
    }

    fun fetchTopRatedTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            topRatedTvShowsUiState = UiState.Loading
            topRatedTvShowsUiState = topRatedTvShowsUseCase().asSuccessOrErrorUiState()
        }
    }

    fun fetchPopularTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            popularTvShowsUiState = UiState.Loading
            popularTvShowsUiState = popularTvShowsUseCase().asSuccessOrErrorUiState()
        }
    }

    fun fetchTodayAiringTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            todayAiringTvShowsUiState = UiState.Loading
            todayAiringTvShowsUiState = todayAiringTvShowsUseCase().asSuccessOrErrorUiState()
        }
    }

    fun fetchNowAiringTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            nowAiringTvShowsUiState = UiState.Loading
            nowAiringTvShowsUiState = nowAiringTvShowsUseCase().asSuccessOrErrorUiState()
        }
    }

    fun fetchUpcomingTvShows(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            upcomingTvShowsUiState = UiState.Loading
            upcomingTvShowsUiState = upcomingTvShowsUseCase().asSuccessOrErrorUiState()
        }
    }
}