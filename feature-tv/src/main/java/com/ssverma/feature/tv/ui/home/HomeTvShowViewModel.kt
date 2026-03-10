package com.ssverma.feature.tv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.usecase.NowAiringTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.PopularTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TodayAiringTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TopRatedTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TrendingTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TvGenresUseCase
import com.ssverma.feature.tv.domain.usecase.UpcomingTvShowsUseCase
import com.ssverma.shared.domain.model.tv.asTvShowPreview
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TimeWindow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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


    private val _uiState = MutableStateFlow(HomeTvUiState())
    val uiState: StateFlow<HomeTvUiState> = _uiState.asStateFlow()

    init {
        fetchAllHomeData()
    }

    fun fetchAllHomeData() {
        fetchTvGenres()
        fetchTrendingTvShows()
        fetchTodayAiringTvShows()
        fetchNowAiringTvShows()
    }

    fun fetchTvGenres() = viewModelScope.launch {
        _uiState.update { it.copy(genres = UiState.Loading) }
        when (val result = tvGenresUseCase()) {
            is Result.Success -> _uiState.update { it.copy(genres = UiState.Success(result.data)) }
            is Result.Error -> _uiState.update { it.copy(genres = UiState.Error(result.error)) }
        }
    }

    fun fetchTrendingTvShows() = viewModelScope.launch {
        _uiState.update { it.copy(trendingTvShows = UiState.Loading) }
        when (val result = trendingTvShowsUseCase(TimeWindow.Daily)) {
            is Result.Success -> {
                val previews = result.data.map { it.asTvShowPreview() }
                _uiState.update { it.copy(trendingTvShows = UiState.Success(previews)) }
            }

            is Result.Error -> _uiState.update { it.copy(trendingTvShows = UiState.Error(result.error)) }
        }
    }

    fun fetchTodayAiringTvShows() = viewModelScope.launch {
        _uiState.update { it.copy(todayAiringTvShows = UiState.Loading) }
        when (val result = todayAiringTvShowsUseCase()) {
            is Result.Success -> {
                val previews = result.data.map { it.asTvShowPreview() }
                _uiState.update { it.copy(todayAiringTvShows = UiState.Success(previews)) }
            }

            is Result.Error -> _uiState.update { it.copy(todayAiringTvShows = UiState.Error(result.error)) }
        }
    }

    fun fetchPopularTvShows() {
        val currentState = _uiState.value.popularTvShows
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        viewModelScope.launch {
            _uiState.update { it.copy(popularTvShows = UiState.Loading) }
            when (val result = popularTvShowsUseCase()) {
                is Result.Success -> {
                    val previews = result.data.map { it.asTvShowPreview() }
                    _uiState.update { it.copy(popularTvShows = UiState.Success(previews)) }
                }

                is Result.Error -> _uiState.update { it.copy(popularTvShows = UiState.Error(result.error)) }
            }
        }
    }

    fun fetchTopRatedTvShows() {
        val currentState = _uiState.value.topRatedTvShows
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        viewModelScope.launch {
            _uiState.update { it.copy(topRatedTvShows = UiState.Loading) }
            when (val result = topRatedTvShowsUseCase()) {
                is Result.Success -> {
                    val previews = result.data.map { it.asTvShowPreview() }
                    _uiState.update { it.copy(topRatedTvShows = UiState.Success(previews)) }
                }

                is Result.Error -> _uiState.update { it.copy(topRatedTvShows = UiState.Error(result.error)) }
            }
        }
    }

    fun fetchUpcomingTvShows() {
        val currentState = _uiState.value.upcomingTvShows
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        viewModelScope.launch {
            _uiState.update { it.copy(upcomingTvShows = UiState.Loading) }
            when (val result = upcomingTvShowsUseCase()) {
                is Result.Success -> {
                    val previews = result.data.map { it.asTvShowPreview() }
                    _uiState.update { it.copy(upcomingTvShows = UiState.Success(previews)) }
                }

                is Result.Error -> _uiState.update { it.copy(upcomingTvShows = UiState.Error(result.error)) }
            }
        }
    }

    fun fetchNowAiringTvShows() {
        val currentState = _uiState.value.nowAiringTvShows
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        viewModelScope.launch {
            _uiState.update { it.copy(nowAiringTvShows = UiState.Loading) }
            when (val result = nowAiringTvShowsUseCase()) {
                is Result.Success -> {
                    val previews = result.data.map { it.asTvShowPreview() }
                    _uiState.update { it.copy(nowAiringTvShows = UiState.Success(previews)) }
                }

                is Result.Error -> _uiState.update { it.copy(nowAiringTvShows = UiState.Error(result.error)) }
            }
        }
    }

}
