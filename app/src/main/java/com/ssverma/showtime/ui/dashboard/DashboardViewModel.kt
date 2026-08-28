package com.ssverma.showtime.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.asSuccessOrErrorUiState
import com.ssverma.core.ui.mapSuccess
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.feature.movie.domain.usecase.PopularMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.TrendingMoviesUseCase
import com.ssverma.feature.tv.domain.usecase.PopularTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TrendingTvShowsUseCase
import com.ssverma.shared.ads.injection.AdInjectionConfig
import com.ssverma.shared.ads.injection.AdPlacement
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.injectAds
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.movie.asMoviePreview
import com.ssverma.shared.domain.model.tv.asTvShowPreview
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.CinemaGameRepository
import com.ssverma.shared.domain.repository.TraktSyncRepository
import com.ssverma.shared.domain.usecase.FetchAllWatchProvidersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val trendingMoviesUseCase: TrendingMoviesUseCase,
    private val trendingTvShowsUseCase: TrendingTvShowsUseCase,
    private val popularMoviesUseCase: PopularMoviesUseCase,
    private val popularTvShowsUseCase: PopularTvShowsUseCase,
    private val fetchAllWatchProvidersUseCase: FetchAllWatchProvidersUseCase,
    private val appConfigRepository: AppConfigRepository,
    private val adConfigProvider: AdConfigProvider,
    private val cinemaGameRepository: CinemaGameRepository,
    private val traktAuthManager: TraktAuthManager,
    private val traktSyncRepository: TraktSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val homeSpotlightAdConfig = AdInjectionConfig(
        placement = AdPlacement.Fixed(positions = listOf(1)),
        style = NativeAdStyle.Carousel
    )

    private val popularCarouselAdConfig = AdInjectionConfig(
        placement = AdPlacement.Fixed(positions = listOf(1)),
        style = NativeAdStyle.Grid
    )

    init {
        viewModelScope.launch {
            combine(
                appConfigRepository.watchProviderRegion,
                appConfigRepository.isTranslationEnabled,
                appConfigRepository.contentLanguage,
                appConfigRepository.preferredOriginalLanguage
            ) { _, _, _, _ -> }.collect {
                _uiState.update { DashboardUiState() }
                fetchAllDashboardData()
            }
        }

        viewModelScope.launch {
            cinemaGameRepository.gameStatsFlow.collect { stats ->
                val isCompleted = cinemaGameRepository.isTodayPuzzleCompleted()
                _uiState.update { it.copy(gameStats = stats, isTodayGameCompleted = isCompleted) }
            }
        }

        viewModelScope.launch {
            traktAuthManager.authState.collectLatest { traktState ->
                val isConnected = traktState is TraktAuthState.Connected
                val token = (traktState as? TraktAuthState.Connected)?.accessToken.orEmpty()
                _uiState.update { it.copy(isTraktConnected = isConnected) }

                traktSyncRepository.getUpNextQueueFlow(token).collectLatest { queue ->
                    _uiState.update { it.copy(upNextQueue = queue) }
                }
            }
        }
    }

    fun fetchAllDashboardData() {
        fetchAllWatchProvidersUseCase.invalidateCache()
        fetchTrendingMedia()
        fetchPopularMovies()
        fetchPopularTvShows()
        fetchWatchProviders()
    }

    fun fetchTrendingMedia() = viewModelScope.launch {
        _uiState.update { it.copy(trendingMedia = UiState.Loading) }
        val movieResult = trendingMoviesUseCase(TimeWindow.Daily)
        val tvResult = trendingTvShowsUseCase(TimeWindow.Daily)

        val spotlightItems = mutableListOf<TrendingSpotlightItem>()

        val movies = (movieResult as? Result.Success)?.data.orEmpty()
        val tvShows = (tvResult as? Result.Success)?.data.orEmpty()

        val maxLen = maxOf(movies.size, tvShows.size)
        for (i in 0 until maxLen) {
            if (i < movies.size) {
                val m = movies[i]
                spotlightItems.add(
                    TrendingSpotlightItem(
                        id = m.id,
                        title = m.title,
                        posterImageUrl = m.posterImageUrl,
                        backdropImageUrl = m.backdropImageUrl,
                        voteAvg = m.voteAvg,
                        displayDate = m.displayReleaseDate,
                        mediaType = MediaType.Movie
                    )
                )
            }
            if (i < tvShows.size) {
                val t = tvShows[i]
                spotlightItems.add(
                    TrendingSpotlightItem(
                        id = t.id,
                        title = t.title,
                        posterImageUrl = t.posterImageUrl,
                        backdropImageUrl = t.backdropImageUrl,
                        voteAvg = t.voteAvg,
                        displayDate = t.displayFirstAirDate,
                        mediaType = MediaType.Tv
                    )
                )
            }
        }

        if (spotlightItems.isNotEmpty()) {
            val injected = spotlightItems.injectAds(
                config = homeSpotlightAdConfig,
                isAdsEnabled = adConfigProvider.isAdsEnabled
            )
            _uiState.update { it.copy(trendingMedia = UiState.Success(injected)) }
        } else if (movieResult is Result.Error) {
            _uiState.update { it.copy(trendingMedia = UiState.Error(movieResult.error)) }
        } else if (tvResult is Result.Error) {
            _uiState.update { it.copy(trendingMedia = UiState.Error(Failure.CoreFailure.UnexpectedFailure)) }
        }
    }

    fun fetchPopularMovies() = viewModelScope.launch {
        _uiState.update { it.copy(popularMovies = UiState.Loading) }
        val result = popularMoviesUseCase()
        _uiState.update {
            it.copy(
                popularMovies = result.asSuccessOrErrorUiState().mapSuccess { movies ->
                    movies.map { movie -> movie.asMoviePreview() }.injectAds(
                        config = popularCarouselAdConfig,
                        isAdsEnabled = adConfigProvider.isAdsEnabled
                    )
                }
            )
        }
    }

    fun fetchPopularTvShows() = viewModelScope.launch {
        _uiState.update { it.copy(popularTvShows = UiState.Loading) }
        val result = popularTvShowsUseCase()
        _uiState.update {
            it.copy(
                popularTvShows = result.asSuccessOrErrorUiState().mapSuccess { tvShows ->
                    tvShows.map { tvShow -> tvShow.asTvShowPreview() }.injectAds(
                        config = popularCarouselAdConfig,
                        isAdsEnabled = adConfigProvider.isAdsEnabled
                    )
                }
            )
        }
    }

    fun fetchWatchProviders() = viewModelScope.launch {
        _uiState.update { it.copy(movieProviders = UiState.Loading, tvProviders = UiState.Loading) }
        val movieResult = fetchAllWatchProvidersUseCase.fetchMovieWatchProviders()
        val tvResult = fetchAllWatchProvidersUseCase.fetchTvWatchProviders()
        _uiState.update {
            it.copy(
                movieProviders = movieResult.asSuccessOrErrorUiState(),
                tvProviders = tvResult.asSuccessOrErrorUiState()
            )
        }
    }

    fun setMovieStreamingSelected(selected: Boolean) {
        _uiState.update { it.copy(isMovieStreamingSelected = selected) }
    }

    fun setMoviePopularSelected(selected: Boolean) {
        _uiState.update { it.copy(isMoviePopularSelected = selected) }
    }

    fun onNativeAdLoaded(nativeAd: NativeAd) {
        _uiState.update { it.copy(nativeAd = nativeAd) }
    }

    fun onCarouselNativeAdLoaded(injectableAd: InjectableAd, nativeAd: NativeAd) {
        _uiState.update { currentState ->
            val updatedTrending =
                (currentState.trendingMedia as? UiState.Success)?.data?.map { item ->
                    if (item is InjectableAd && item.id == injectableAd.id) {
                        item.copy(ad = nativeAd)
                    } else {
                        item
                    }
                }
            currentState.copy(
                trendingMedia = updatedTrending?.let { UiState.Success(it) }
                    ?: currentState.trendingMedia
            )
        }
    }

    fun onPopularAdLoaded(injectableAd: InjectableAd, nativeAd: NativeAd) {
        _uiState.update { currentState ->
            val updatedMovies =
                (currentState.popularMovies as? UiState.Success)?.data?.map { item ->
                    if (item is InjectableAd && item.id == injectableAd.id) {
                        item.copy(ad = nativeAd)
                    } else {
                        item
                    }
                }
            val updatedTv =
                (currentState.popularTvShows as? UiState.Success)?.data?.map { item ->
                    if (item is InjectableAd && item.id == injectableAd.id) {
                        item.copy(ad = nativeAd)
                    } else {
                        item
                    }
                }
            currentState.copy(
                popularMovies = updatedMovies?.let { UiState.Success(it) }
                    ?: currentState.popularMovies,
                popularTvShows = updatedTv?.let { UiState.Success(it) }
                    ?: currentState.popularTvShows
            )
        }
    }

    fun markEpisodeWatched(showTmdbId: Int, season: Int, episode: Int) = viewModelScope.launch {
        val traktState = traktAuthManager.authState.value
        val token = (traktState as? TraktAuthState.Connected)?.accessToken

        val currentQueue = _uiState.value.upNextQueue
        val targetItem = currentQueue.find { it.showTmdbId == showTmdbId }
        val wasFinalEpisode =
            targetItem != null && (targetItem.totalCompleted + 1 >= targetItem.totalAired)

        val updatedQueue = currentQueue.map { item ->
            if (item.showTmdbId == showTmdbId) {
                val nextSeasonCompleted = item.seasonCompleted + 1
                val isSeasonFinished =
                    item.seasonTotalAired > 0 && nextSeasonCompleted >= item.seasonTotalAired
                val nextTotalCompleted = item.totalCompleted + 1

                item.copy(
                    seasonCompleted = nextSeasonCompleted.coerceAtMost(if (item.seasonTotalAired > 0) item.seasonTotalAired else nextSeasonCompleted),
                    totalCompleted = nextTotalCompleted.coerceAtMost(item.totalAired),
                    episodeNumber = if (isSeasonFinished) 1 else item.episodeNumber + 1,
                    seasonNumber = if (isSeasonFinished) item.seasonNumber + 1 else item.seasonNumber,
                    episodeTitle = null
                )
            } else item
        }
        _uiState.update { it.copy(upNextQueue = updatedQueue) }

        traktSyncRepository.markEpisodeWatched(
            accessToken = token,
            showTmdbId = showTmdbId,
            season = season,
            episode = episode,
            showTitle = targetItem?.showTitle.orEmpty(),
            showPosterPath = targetItem?.showPosterPath,
            totalAired = targetItem?.totalAired ?: 0
        )

        if (targetItem != null && (targetItem.totalCompleted + 1 >= targetItem.totalAired)) {
            _uiState.update {
                it.copy(
                    completedShowDialog = com.ssverma.shared.domain.model.trakt.CompletedShowDialogState(
                        showTmdbId = targetItem.showTmdbId,
                        showTitle = targetItem.showTitle,
                        showPosterPath = targetItem.showPosterPath,
                        seasonNumber = targetItem.seasonNumber,
                        totalCompleted = targetItem.totalAired,
                        totalAired = targetItem.totalAired
                    )
                )
            }
        }
    }

    fun dismissCompletedShowDialog() {
        val completedShow = _uiState.value.completedShowDialog
        _uiState.update { state ->
            state.copy(
                completedShowDialog = null,
                upNextQueue = if (completedShow != null) {
                    state.upNextQueue.filter { it.showTmdbId != completedShow.showTmdbId }
                } else {
                    state.upNextQueue
                }
            )
        }
    }
}
