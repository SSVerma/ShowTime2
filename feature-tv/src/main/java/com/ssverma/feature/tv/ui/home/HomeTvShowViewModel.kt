package com.ssverma.feature.tv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.mapSuccess
import com.ssverma.feature.tv.domain.usecase.NowAiringTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.PopularTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TodayAiringTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TopRatedTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TrendingTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TvGenresUseCase
import com.ssverma.feature.tv.domain.usecase.UpcomingTvShowsUseCase
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.AdInjectionConfig
import com.ssverma.shared.ads.injection.AdPlacement
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.injectAds
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.model.tv.asTvShowPreview
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
class HomeTvShowViewModel @Inject constructor(
    private val trendingTvShowsUseCase: TrendingTvShowsUseCase,
    private val topRatedTvShowsUseCase: TopRatedTvShowsUseCase,
    private val upcomingTvShowsUseCase: UpcomingTvShowsUseCase,
    private val todayAiringTvShowsUseCase: TodayAiringTvShowsUseCase,
    private val nowAiringTvShowsUseCase: NowAiringTvShowsUseCase,
    private val popularTvShowsUseCase: PopularTvShowsUseCase,
    private val tvGenresUseCase: TvGenresUseCase,
    private val fetchAllWatchProvidersUseCase: FetchAllWatchProvidersUseCase,
    private val appConfigRepository: AppConfigRepository,
    private val adConfigProvider: AdConfigProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeTvUiState())
    val uiState: StateFlow<HomeTvUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appConfigRepository.watchProviderRegion,
                appConfigRepository.isTranslationEnabled,
                appConfigRepository.contentLanguage,
                appConfigRepository.preferredOriginalLanguage
            ) { _, _, _, _ -> }.collect {
                _uiState.update { HomeTvUiState() } // Reset state to trigger re-fetch of all sections
                fetchAllHomeData()
            }
        }
    }

    fun fetchAllHomeData() {
        tvGenresUseCase.invalidateCache()
        fetchAllWatchProvidersUseCase.invalidateCache()
        fetchTvGenres()
        fetchTrendingTvShows()
        fetchTodayAiringTvShows()
        fetchNowAiringTvShows()
        fetchWatchProviders()
    }

    private val homeAdConfig = AdInjectionConfig(
        placement = AdPlacement.Fixed(positions = listOf(1)),
        style = NativeAdStyle.Grid
    )

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
                _uiState.update {
                    it.copy(
                        trendingTvShows = UiState.Success(
                            result.data
                                .distinctBy { t -> t.id } // Safety Filter
                                .map { t -> t.asTvShowPreview() }
                                .injectAds(
                                    config = homeAdConfig.copy(style = NativeAdStyle.Carousel),
                                    isAdsEnabled = adConfigProvider.isAdsEnabled
                                )
                        )
                    )
                }
            }

            is Result.Error -> _uiState.update { it.copy(trendingTvShows = UiState.Error(result.error)) }
        }
    }

    fun fetchTodayAiringTvShows() = viewModelScope.launch {
        _uiState.update { it.copy(todayAiringTvShows = UiState.Loading) }
        when (val result = todayAiringTvShowsUseCase()) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        todayAiringTvShows = UiState.Success(
                            result.data
                                .distinctBy { t -> t.id }
                                .map { t -> t.asTvShowPreview() }
                                .take(5)
                                .injectAds(
                                    config = homeAdConfig.copy(style = NativeAdStyle.List),
                                    isAdsEnabled = adConfigProvider.isAdsEnabled
                                )
                        )
                    )
                }
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
                    _uiState.update {
                        it.copy(
                            popularTvShows = UiState.Success(
                                result.data
                                    .distinctBy { t -> t.id }
                                    .map { t -> t.asTvShowPreview() }
                                    .injectAds(homeAdConfig)
                            )
                        )
                    }
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
                    _uiState.update {
                        it.copy(
                            topRatedTvShows = UiState.Success(
                                result.data
                                    .distinctBy { t -> t.id }
                                    .map { t -> t.asTvShowPreview() }
                                    .injectAds(homeAdConfig)
                            )
                        )
                    }
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
                    val sortedTvShows = result.data
                        .distinctBy { t -> t.id }
                        .sortedWith(compareBy(nullsLast()) { it.firstAirDate })

                    _uiState.update {
                        it.copy(
                            upcomingTvShows = UiState.Success(
                                sortedTvShows
                                    .map { t -> t.asTvShowPreview() }
                                    .injectAds(homeAdConfig)
                            )
                        )
                    }
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
                    _uiState.update {
                        it.copy(
                            nowAiringTvShows = UiState.Success(
                                result.data
                                    .distinctBy { t -> t.id }
                                    .map { t -> t.asTvShowPreview() }
                                    .injectAds(
                                        config = homeAdConfig.copy(style = NativeAdStyle.List),
                                        isAdsEnabled = adConfigProvider.isAdsEnabled
                                    )
                            )
                        )
                    }
                }

                is Result.Error -> _uiState.update { it.copy(nowAiringTvShows = UiState.Error(result.error)) }
            }
        }
    }

    fun onNativeAdLoaded(
        injectableAd: InjectableAd,
        nativeAd: NativeAd
    ) {
        // 1. Anti-Loop Shield: If this ad is already set, do absolutely nothing.
        if (injectableAd.ad === nativeAd) return

        _uiState.update { currentState ->

            // 2. Smart Updater: Operates purely on the List to prevent full-screen recomposition
            fun <T> List<AdInjectable<T>>.updateAdIfPresent(): List<AdInjectable<T>> {
                var adFound = false

                val updatedList = this.map { item ->
                    if (item is InjectableAd && item.id == injectableAd.id) {
                        adFound = true
                        item.copy(ad = nativeAd)
                    } else {
                        item
                    }
                }

                // If the ad wasn't in this list, return the EXACT original list reference.
                return if (adFound) updatedList else this
            }

            currentState.copy(
                trendingTvShows = currentState.trendingTvShows.mapSuccess { it.updateAdIfPresent() },
                todayAiringTvShows = currentState.todayAiringTvShows.mapSuccess { it.updateAdIfPresent() },
                popularTvShows = currentState.popularTvShows.mapSuccess { it.updateAdIfPresent() },
                topRatedTvShows = currentState.topRatedTvShows.mapSuccess { it.updateAdIfPresent() },
                upcomingTvShows = currentState.upcomingTvShows.mapSuccess { it.updateAdIfPresent() },
                nowAiringTvShows = currentState.nowAiringTvShows.mapSuccess { it.updateAdIfPresent() }
            )
        }
    }

    fun fetchWatchProviders() = viewModelScope.launch {
        _uiState.update { it.copy(watchProviders = UiState.Loading) }
        when (val result = fetchAllWatchProvidersUseCase.fetchTvWatchProviders()) {
            is Result.Success -> _uiState.update { it.copy(watchProviders = UiState.Success(result.data)) }
            is Result.Error -> _uiState.update { it.copy(watchProviders = UiState.Error(result.error)) }
        }
    }

    fun onFeedInlineAdLoaded(nativeAd: NativeAd) {
        _uiState.update { it.copy(feedInlineAd = nativeAd) }
    }

    fun onWatchProviderAdLoaded(nativeAd: NativeAd) {
        _uiState.update { it.copy(watchProviderAd = nativeAd) }
    }
}
