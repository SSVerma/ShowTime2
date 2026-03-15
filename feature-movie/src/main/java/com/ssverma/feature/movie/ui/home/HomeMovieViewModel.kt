package com.ssverma.feature.movie.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.usecase.InCinemaMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.MovieGenresUseCase
import com.ssverma.feature.movie.domain.usecase.PopularMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.TopRatedMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.TrendingMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.UpcomingMoviesUseCase
import com.ssverma.shared.domain.model.movie.asMoviePreview
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
class HomeMovieViewModel @Inject constructor(
    private val trendingMoviesUseCase: TrendingMoviesUseCase,
    private val topRatedMoviesUseCase: TopRatedMoviesUseCase,
    private val upcomingMoviesUseCase: UpcomingMoviesUseCase,
    private val inCinemaMoviesUseCase: InCinemaMoviesUseCase,
    private val popularMoviesUseCase: PopularMoviesUseCase,
    private val movieGenreUseCase: MovieGenresUseCase,
    private val appConfigRepository: com.ssverma.shared.domain.repository.AppConfigRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appConfigRepository.watchProviderRegion.collect {
                fetchAllHomeData()
            }
        }
    }

    fun fetchAllHomeData() {
        val region = appConfigRepository.watchProviderRegion.value
        fetchMovieGenres()
        fetchTrendingMovies()
        fetchInCinemaMovies(region)
    }

    fun fetchMovieGenres() = viewModelScope.launch {
        _uiState.update { it.copy(genres = UiState.Loading) }
        when (val result = movieGenreUseCase()) {
            is Result.Success -> _uiState.update { it.copy(genres = UiState.Success(result.data)) }
            is Result.Error -> _uiState.update { it.copy(genres = UiState.Error(result.error)) }
        }
    }

    fun fetchTrendingMovies() = viewModelScope.launch {
        _uiState.update { it.copy(trendingMovies = UiState.Loading) }
        when (val result = trendingMoviesUseCase(TimeWindow.Daily)) {
            is Result.Success -> {
                val previews = result.data.map { it.asMoviePreview() }
                _uiState.update { it.copy(trendingMovies = UiState.Success(previews)) }
            }

            is Result.Error -> _uiState.update { it.copy(trendingMovies = UiState.Error(result.error)) }
        }
    }

    fun fetchInCinemaMovies(region: String? = null) = viewModelScope.launch {
        _uiState.update { it.copy(inCinemasMovies = UiState.Loading) }
        when (val result = inCinemaMoviesUseCase(region)) {
            is Result.Success -> {
                val previews = result.data.map { it.asMoviePreview() }
                _uiState.update { it.copy(inCinemasMovies = UiState.Success(previews)) }
            }

            is Result.Error -> _uiState.update { it.copy(inCinemasMovies = UiState.Error(result.error)) }
        }
    }

    fun fetchPopularMovies() {
        val currentState = _uiState.value.popularMovies
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        val region = appConfigRepository.watchProviderRegion.value

        viewModelScope.launch {
            _uiState.update { it.copy(popularMovies = UiState.Loading) }
            when (val result = popularMoviesUseCase(region)) {
                is Result.Success -> {
                    val previews = result.data.map { it.asMoviePreview() }
                    _uiState.update { it.copy(popularMovies = UiState.Success(previews)) }
                }

                is Result.Error -> _uiState.update { it.copy(popularMovies = UiState.Error(result.error)) }
            }
        }
    }

    fun fetchTopRatedMovies() {
        val currentState = _uiState.value.topRatedMovies
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        val region = appConfigRepository.watchProviderRegion.value

        viewModelScope.launch {
            _uiState.update { it.copy(topRatedMovies = UiState.Loading) }
            when (val result = topRatedMoviesUseCase(region)) {
                is Result.Success -> {
                    val previews = result.data.map { it.asMoviePreview() }
                    _uiState.update { it.copy(topRatedMovies = UiState.Success(previews)) }
                }

                is Result.Error -> _uiState.update { it.copy(topRatedMovies = UiState.Error(result.error)) }
            }
        }
    }

    fun fetchUpcomingMovies() {
        val currentState = _uiState.value.upcomingMovies
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        val region = appConfigRepository.watchProviderRegion.value

        viewModelScope.launch {
            _uiState.update { it.copy(upcomingMovies = UiState.Loading) }
            when (val result = upcomingMoviesUseCase(region)) {
                is Result.Success -> {
                    val previews = result.data.map { it.asMoviePreview() }
                    _uiState.update { it.copy(upcomingMovies = UiState.Success(previews)) }
                }

                is Result.Error -> _uiState.update { it.copy(upcomingMovies = UiState.Error(result.error)) }
            }
        }
    }

}
