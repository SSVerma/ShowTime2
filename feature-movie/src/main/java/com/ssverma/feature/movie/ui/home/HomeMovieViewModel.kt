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
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.usecase.FetchAllWatchProvidersUseCase
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.movie.asMoviePreview
import com.ssverma.shared.domain.model.DiscoveryParams
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.repository.AppConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val fetchAllWatchProvidersUseCase: FetchAllWatchProvidersUseCase,
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appConfigRepository.watchProviderRegion,
                appConfigRepository.isTranslationEnabled,
                appConfigRepository.contentLanguage,
                appConfigRepository.preferredOriginalLanguage
            ) { _, _, _, _ -> }.collect {
                _uiState.update { HomeUiState() } // Reset state to trigger re-fetch of all sections
                fetchAllHomeData()
            }
        }
    }

    fun fetchAllHomeData() {
        movieGenreUseCase.invalidateCache()
        val discoveryParams = DiscoveryParams(
            region = appConfigRepository.watchProviderRegion.value,
            originalLanguage = appConfigRepository.preferredOriginalLanguage.value
        )
        fetchMovieGenres()
        fetchTrendingMovies()
        fetchInCinemaMovies(discoveryParams)
        fetchWatchProviders()
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
                _uiState.update { 
                    it.copy(trendingMovies = UiState.Success(result.data.map { m -> m.asMoviePreview() }))
                }
            }
            is Result.Error -> _uiState.update { it.copy(trendingMovies = UiState.Error(result.error)) }
        }
    }

    fun fetchInCinemaMovies(discoveryParams: DiscoveryParams? = null) = viewModelScope.launch {
        _uiState.update { it.copy(inCinemasMovies = UiState.Loading) }
        when (val result = inCinemaMoviesUseCase(discoveryParams)) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(inCinemasMovies = UiState.Success(result.data.map { m -> m.asMoviePreview() }))
                }
            }
            is Result.Error -> _uiState.update { it.copy(inCinemasMovies = UiState.Error(result.error)) }
        }
    }

    fun fetchPopularMovies() {
        val currentState = _uiState.value.popularMovies
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        val discoveryParams = DiscoveryParams(
            region = appConfigRepository.watchProviderRegion.value,
            originalLanguage = appConfigRepository.preferredOriginalLanguage.value
        )

        viewModelScope.launch {
            _uiState.update { it.copy(popularMovies = UiState.Loading) }
            when (val result = popularMoviesUseCase(discoveryParams)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(popularMovies = UiState.Success(result.data.map { m -> m.asMoviePreview() }))
                    }
                }
                is Result.Error -> _uiState.update { it.copy(popularMovies = UiState.Error(result.error)) }
            }
        }
    }

    fun fetchTopRatedMovies() {
        val currentState = _uiState.value.topRatedMovies
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        val discoveryParams = DiscoveryParams(
            region = appConfigRepository.watchProviderRegion.value,
            originalLanguage = appConfigRepository.preferredOriginalLanguage.value
        )

        viewModelScope.launch {
            _uiState.update { it.copy(topRatedMovies = UiState.Loading) }
            when (val result = topRatedMoviesUseCase(discoveryParams)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(topRatedMovies = UiState.Success(result.data.map { m -> m.asMoviePreview() }))
                    }
                }
                is Result.Error -> _uiState.update { it.copy(topRatedMovies = UiState.Error(result.error)) }
            }
        }
    }

    fun fetchUpcomingMovies() {
        val currentState = _uiState.value.upcomingMovies
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        val discoveryParams = DiscoveryParams(
            region = appConfigRepository.watchProviderRegion.value,
            originalLanguage = appConfigRepository.preferredOriginalLanguage.value
        )

        viewModelScope.launch {
            _uiState.update { it.copy(upcomingMovies = UiState.Loading) }
            when (val result = upcomingMoviesUseCase(discoveryParams)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(upcomingMovies = UiState.Success(result.data.map { m -> m.asMoviePreview() }))
                    }
                }
                is Result.Error -> _uiState.update { it.copy(upcomingMovies = UiState.Error(result.error)) }
            }
        }
    }

    fun fetchWatchProviders() = viewModelScope.launch {
        _uiState.update { it.copy(watchProviders = UiState.Loading) }

        when (val result = fetchAllWatchProvidersUseCase.fetchMovieWatchProviders()) {
            is Result.Success -> _uiState.update { it.copy(watchProviders = UiState.Success(result.data)) }
            is Result.Error -> _uiState.update { it.copy(watchProviders = UiState.Error(result.error)) }
        }
    }

}
