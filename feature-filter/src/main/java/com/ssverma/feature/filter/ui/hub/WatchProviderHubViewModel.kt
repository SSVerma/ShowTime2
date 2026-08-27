package com.ssverma.feature.filter.ui.hub

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.ui.UiState
import com.ssverma.feature.filter.ui.hub.config.MovieHubDiscoverConfig
import com.ssverma.feature.filter.ui.hub.config.TvHubDiscoverConfig
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.AdInjectionConfig
import com.ssverma.shared.ads.injection.AdPlacement
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.injectAds
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.asMoviePreview
import com.ssverma.shared.domain.model.tv.asTvShowPreview
import com.ssverma.shared.domain.repository.DiscoveryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = WatchProviderHubViewModel.Factory::class)
class WatchProviderHubViewModel @AssistedInject constructor(
    private val discoveryRepository: DiscoveryRepository,
    private val adConfigProvider: AdConfigProvider,
    @Assisted("providerId") val providerId: Int,
    @Assisted("providerName") val providerName: String,
    @Assisted("logoPath") private val logoPath: String,
    @Assisted("isMovie") private val isMovie: Boolean
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("providerId") providerId: Int,
            @Assisted("providerName") providerName: String,
            @Assisted("logoPath") logoPath: String,
            @Assisted("isMovie") isMovie: Boolean
        ): WatchProviderHubViewModel
    }

    private val hubCarouselAdConfig = AdInjectionConfig(
        placement = AdPlacement.Fixed(positions = listOf(1)),
        style = NativeAdStyle.Grid
    )

    private var cachedMovieHub: HubContent? = null
    private var cachedTvHub: HubContent? = null

    private val _uiState = MutableStateFlow(
        WatchProviderHubUiState(
            provider = ProviderInfo(
                providerId = providerId,
                providerName = providerName,
                logoPath = Uri.decode(logoPath),
                displayPriority = 0
            ),
            isMovieMode = isMovie,
            hubContentState = UiState.Loading
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        fetchHubContent()
    }

    fun toggleMode(isMovieMode: Boolean) {
        if (_uiState.value.isMovieMode == isMovieMode) return
        _uiState.update { it.copy(isMovieMode = isMovieMode) }

        val cached = if (isMovieMode) cachedMovieHub else cachedTvHub
        if (cached != null) {
            _uiState.update { it.copy(hubContentState = UiState.Success(cached)) }
        } else {
            fetchHubContent()
        }
    }

    fun fetchHubContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(hubContentState = UiState.Loading) }
            if (_uiState.value.isMovieMode) {
                fetchMovieHub()
            } else {
                fetchTvHub()
            }
        }
    }

    fun onCarouselNativeAdLoaded(injectableAd: InjectableAd, nativeAd: NativeAd) {
        _uiState.update { currentState ->
            val content = (currentState.hubContentState as? UiState.Success)?.data
                ?: return@update currentState

            fun updateList(list: List<AdInjectable<MediaPreview>>): List<AdInjectable<MediaPreview>> {
                return list.map { item ->
                    if (item is InjectableAd && item.id == injectableAd.id) {
                        item.copy(ad = nativeAd)
                    } else item
                }
            }

            val updatedContent = content.copy(
                heroItems = updateList(content.heroItems),
                newItems = updateList(content.newItems),
                upcomingItems = updateList(content.upcomingItems),
                topRatedItems = updateList(content.topRatedItems)
            )

            if (currentState.isMovieMode) {
                cachedMovieHub = updatedContent
            } else {
                cachedTvHub = updatedContent
            }

            currentState.copy(hubContentState = UiState.Success(updatedContent))
        }
    }

    private suspend fun fetchMovieHub() {
        val heroDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieHubDiscoverConfig.heroItems(providerId = providerId)
            )
        }

        val newDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieHubDiscoverConfig.newReleases(providerId = providerId)
            )
        }

        val upcomingDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieHubDiscoverConfig.upcoming(providerId = providerId)
            )
        }

        val topDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieHubDiscoverConfig.topRated(providerId = providerId)
            )
        }

        val genresDeferred = viewModelScope.async {
            discoveryRepository.fetchMovieGenre()
        }

        val heroResult = heroDeferred.await()
        val newResult = newDeferred.await()
        val upcomingResult = upcomingDeferred.await()
        val ratedResult = topDeferred.await()
        val genresResult = genresDeferred.await()

        val isAds = adConfigProvider.isAdsEnabled

        val rawHero: List<MediaPreview> = heroResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }
        val rawNew: List<MediaPreview> = newResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }
        val rawUpcoming: List<MediaPreview> = upcomingResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }
        val rawRated: List<MediaPreview> = ratedResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }

        val heroItems = rawHero.injectAds(
            config = hubCarouselAdConfig.copy(style = NativeAdStyle.Carousel),
            isAdsEnabled = isAds
        )
        val newItems = rawNew.injectAds(config = hubCarouselAdConfig, isAdsEnabled = isAds)
        val upcomingItems =
            rawUpcoming.injectAds(config = hubCarouselAdConfig, isAdsEnabled = isAds)
        val ratedItems = rawRated.injectAds(config = hubCarouselAdConfig, isAdsEnabled = isAds)

        val genres = genresResult.getOrDefault(emptyList())

        // Show error only if all primary sections fail
        if (rawHero.isEmpty() && rawNew.isEmpty() && rawUpcoming.isEmpty() && rawRated.isEmpty()) {
            val error = listOf(heroResult, newResult, upcomingResult, ratedResult)
                .filterIsInstance<Result.Error<Failure.CoreFailure>>()
                .firstOrNull()?.error ?: Failure.CoreFailure.UnexpectedFailure

            _uiState.update { it.copy(hubContentState = UiState.Error(error)) }
            return
        }

        val content = HubContent(
            heroItems = heroItems,
            newItems = newItems,
            upcomingItems = upcomingItems,
            topRatedItems = ratedItems,
            genres = genres
        )
        cachedMovieHub = content
        _uiState.update { it.copy(hubContentState = UiState.Success(content)) }
    }

    private suspend fun fetchTvHub() {
        val heroDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvHubDiscoverConfig.heroItems(providerId = providerId)
            )
        }

        val newDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvHubDiscoverConfig.newReleases(providerId = providerId)
            )
        }

        val upcomingDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvHubDiscoverConfig.upcoming(providerId = providerId)
            )
        }

        val ratedDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvHubDiscoverConfig.topRated(providerId = providerId)
            )
        }

        val genresDeferred = viewModelScope.async {
            discoveryRepository.fetchTvShowGenre()
        }

        val heroResult = heroDeferred.await()
        val newResult = newDeferred.await()
        val upcomingResult = upcomingDeferred.await()
        val ratedResult = ratedDeferred.await()
        val genresResult = genresDeferred.await()

        val isAds = adConfigProvider.isAdsEnabled

        val rawHero: List<MediaPreview> = heroResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }
        val rawNew: List<MediaPreview> = newResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }
        val rawUpcoming: List<MediaPreview> = upcomingResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }
        val rawRated: List<MediaPreview> = ratedResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }

        val heroItems = rawHero.injectAds(
            config = hubCarouselAdConfig.copy(style = NativeAdStyle.Carousel),
            isAdsEnabled = isAds
        )
        val newItems = rawNew.injectAds(config = hubCarouselAdConfig, isAdsEnabled = isAds)
        val upcomingItems =
            rawUpcoming.injectAds(config = hubCarouselAdConfig, isAdsEnabled = isAds)
        val ratedItems = rawRated.injectAds(config = hubCarouselAdConfig, isAdsEnabled = isAds)

        val genres = genresResult.getOrDefault(emptyList())

        if (rawHero.isEmpty() && rawNew.isEmpty() && rawUpcoming.isEmpty() && rawRated.isEmpty()) {
            val error = listOf(heroResult, newResult, upcomingResult, ratedResult)
                .filterIsInstance<Result.Error<Failure.CoreFailure>>()
                .firstOrNull()?.error ?: Failure.CoreFailure.UnexpectedFailure

            _uiState.update { it.copy(hubContentState = UiState.Error(error)) }
            return
        }

        val content = HubContent(
            heroItems = heroItems,
            newItems = newItems,
            upcomingItems = upcomingItems,
            topRatedItems = ratedItems,
            genres = genres
        )
        cachedTvHub = content
        _uiState.update { it.copy(hubContentState = UiState.Success(content)) }
    }

    private fun <T> Result<T, Failure.CoreFailure>.getOrDefault(default: T): T {
        return if (this is Result.Success) this.data else default
    }
}
