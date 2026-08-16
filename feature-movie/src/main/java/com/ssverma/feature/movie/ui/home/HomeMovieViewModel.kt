package com.ssverma.feature.movie.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.mapSuccess
import com.ssverma.feature.movie.domain.usecase.InCinemaMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.MovieGenresUseCase
import com.ssverma.feature.movie.domain.usecase.PopularMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.TopRatedMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.TrendingMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.UpcomingMoviesUseCase
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.AdInjectionConfig
import com.ssverma.shared.ads.injection.AdPlacement
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.injectAds
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.model.movie.asMoviePreview
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.usecase.FetchAllWatchProvidersUseCase
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
    private val appConfigRepository: AppConfigRepository,
    private val adConfigProvider: AdConfigProvider
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
        fetchAllWatchProvidersUseCase.invalidateCache()
        fetchMovieGenres()
        fetchTrendingMovies()
        fetchInCinemaMovies()
        fetchWatchProviders()
    }

    private val homeAdConfig = AdInjectionConfig(
        placement = AdPlacement.Fixed(positions = listOf(1)),
        style = NativeAdStyle.Grid
    )

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
                    it.copy(
                        trendingMovies = UiState.Success(
                            data = result.data.map { m -> m.asMoviePreview() }
                                .injectAds(
                                    homeAdConfig.copy(style = NativeAdStyle.Carousel),
                                    isAdsEnabled = adConfigProvider.isAdsEnabled
                                )
                        )
                    )
                }
            }

            is Result.Error -> _uiState.update { it.copy(trendingMovies = UiState.Error(result.error)) }
        }
    }

    fun fetchInCinemaMovies() = viewModelScope.launch {
        _uiState.update { it.copy(inCinemasMovies = UiState.Loading) }
        when (val result = inCinemaMoviesUseCase()) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        inCinemasMovies = UiState.Success(
                            data = result.data.map { m -> m.asMoviePreview() }
                                .take(5)
                                .injectAds(
                                    homeAdConfig.copy(style = NativeAdStyle.List),
                                    isAdsEnabled = adConfigProvider.isAdsEnabled
                                )
                        )
                    )
                }
            }

            is Result.Error -> _uiState.update { it.copy(inCinemasMovies = UiState.Error(result.error)) }
        }
    }

    fun fetchPopularMovies() {
        val currentState = _uiState.value.popularMovies
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        viewModelScope.launch {
            _uiState.update { it.copy(popularMovies = UiState.Loading) }
            when (val result = popularMoviesUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            popularMovies = UiState.Success(
                                result.data.map { m -> m.asMoviePreview() }.injectAds(
                                    homeAdConfig,
                                    isAdsEnabled = adConfigProvider.isAdsEnabled
                                )
                            )
                        )
                    }
                }

                is Result.Error -> _uiState.update { it.copy(popularMovies = UiState.Error(result.error)) }
            }
        }
    }

    fun fetchTopRatedMovies() {
        val currentState = _uiState.value.topRatedMovies
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        viewModelScope.launch {
            _uiState.update { it.copy(topRatedMovies = UiState.Loading) }
            when (val result = topRatedMoviesUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            topRatedMovies = UiState.Success(
                                result.data.map { m -> m.asMoviePreview() }.injectAds(
                                    homeAdConfig,
                                    isAdsEnabled = adConfigProvider.isAdsEnabled
                                )
                            )
                        )
                    }
                }

                is Result.Error -> _uiState.update { it.copy(topRatedMovies = UiState.Error(result.error)) }
            }
        }
    }

    fun fetchUpcomingMovies() {
        val currentState = _uiState.value.upcomingMovies
        if (currentState is UiState.Loading || currentState is UiState.Success) return

        viewModelScope.launch {
            _uiState.update { it.copy(upcomingMovies = UiState.Loading) }
            when (val result = upcomingMoviesUseCase()) {
                is Result.Success -> {
                    val sortedMovies = result.data
                        .distinctBy { m -> m.id }
                        .sortedWith(compareBy(nullsLast()) { it.releaseDate })

                    _uiState.update {
                        it.copy(
                            upcomingMovies = UiState.Success(
                                sortedMovies.map { m -> m.asMoviePreview() }
                                    .injectAds(
                                        config = homeAdConfig,
                                        isAdsEnabled = adConfigProvider.isAdsEnabled
                                    )
                            )
                        )
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

    fun onNativeAdLoaded(
        injectableAd: InjectableAd,
        nativeAd: NativeAd
    ) {
        // Anti-Loop Shield
        if (injectableAd.ad === nativeAd) return

        _uiState.update { currentState ->

            fun <T> List<AdInjectable<T>>.updateAdIfPresent(): List<AdInjectable<T>> {
                var adFound = false

                val updatedList = this.map { item ->
                    if (item is InjectableAd && item.id == injectableAd.id) {
                        adFound = true
                        item.copy(ad = nativeAd) // Update this specific ad!
                    } else {
                        item // Leave standard content (or other ads) completely alone
                    }
                }

                return if (adFound) updatedList else this
            }

            currentState.copy(
                trendingMovies = currentState.trendingMovies.mapSuccess { it.updateAdIfPresent() },
                inCinemasMovies = currentState.inCinemasMovies.mapSuccess { it.updateAdIfPresent() },
                popularMovies = currentState.popularMovies.mapSuccess { it.updateAdIfPresent() },
                topRatedMovies = currentState.topRatedMovies.mapSuccess { it.updateAdIfPresent() },
                upcomingMovies = currentState.upcomingMovies.mapSuccess { it.updateAdIfPresent() }
            )
        }
    }

    fun onFeedInlineAdLoaded(nativeAd: NativeAd) {
        _uiState.update { it.copy(feedInlineAd = nativeAd) }
    }
}
