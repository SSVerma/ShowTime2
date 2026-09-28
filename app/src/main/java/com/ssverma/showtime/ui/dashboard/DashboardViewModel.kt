package com.ssverma.showtime.ui.dashboard

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.asSuccessOrErrorUiState
import com.ssverma.core.ui.mapSuccess
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.movie.domain.usecase.MovieGenresUseCase
import com.ssverma.feature.movie.domain.usecase.PopularMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.TrendingMoviesUseCase
import com.ssverma.feature.tv.domain.usecase.PopularTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TrendingTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TvGenresUseCase
import com.ssverma.shared.ads.injection.AdInjectionConfig
import com.ssverma.shared.ads.injection.AdPlacement
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.injectAds
import com.ssverma.shared.ads.injection.removeNativeAd
import com.ssverma.shared.ads.injection.updateNativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.auth.TraktAuthState
import com.ssverma.shared.domain.model.community.CloneCommunityListParams
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.community.ToggleListUpvoteParams
import com.ssverma.shared.domain.model.feature.CinephileFeature
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.movie.asMoviePreview
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.trakt.CompletedShowDialogState
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.domain.model.tv.asTvShowPreview
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.CinemaGameRepository
import com.ssverma.shared.domain.repository.ReminderRepository
import com.ssverma.shared.domain.repository.TraktSyncRepository
import com.ssverma.shared.domain.usecase.FetchAllWatchProvidersUseCase
import com.ssverma.shared.domain.usecase.community.CloneCommunityListUseCase
import com.ssverma.shared.domain.usecase.community.GetCommunityListsUseCase
import com.ssverma.shared.domain.usecase.community.GetDailyPollUseCase
import com.ssverma.shared.domain.usecase.community.GetTrendingDiscussionsUseCase
import com.ssverma.shared.domain.usecase.community.IsTodayPollVotedUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommunityListUpvoteUseCase
import com.ssverma.shared.domain.usecase.community.VoteDailyPollUseCase
import com.ssverma.shared.domain.usecase.library.GetCustomListsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
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
    private val traktSyncRepository: TraktSyncRepository,
    private val getDailyPollUseCase: GetDailyPollUseCase,
    private val isTodayPollVotedUseCase: IsTodayPollVotedUseCase,
    private val voteDailyPollUseCase: VoteDailyPollUseCase,
    private val getTrendingDiscussionsUseCase: GetTrendingDiscussionsUseCase,
    private val getCommunityListsUseCase: GetCommunityListsUseCase,
    private val getCustomListsUseCase: GetCustomListsUseCase,
    private val toggleCommunityListUpvoteUseCase: ToggleCommunityListUpvoteUseCase,
    private val cloneCommunityListUseCase: CloneCommunityListUseCase,
    private val movieGenresUseCase: MovieGenresUseCase,
    private val tvGenresUseCase: TvGenresUseCase,
    private val reminderRepository: ReminderRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    val isPro: StateFlow<Boolean> = billingRepository.isProActive

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

    private var rawSpotlightItems: List<TrendingSpotlightItem> = emptyList()
    private var rawPopularMovies: List<MoviePreview> = emptyList()
    private var rawPopularTvShows: List<TvShowPreview> = emptyList()

    private fun isAdsCurrentlyEnabled(): Boolean {
        return adConfigProvider.isAdsEnabled && !billingRepository.isProActive.value
    }

    private fun applyAdInjection() {
        val adsEnabled = isAdsCurrentlyEnabled()

        if (rawSpotlightItems.isNotEmpty()) {
            val injected = rawSpotlightItems.injectAds(
                config = homeSpotlightAdConfig.copy(sectionTag = "dashboard_spotlight"),
                isAdsEnabled = adsEnabled
            )
            _uiState.update { it.copy(trendingMedia = UiState.Success(injected)) }
        }

        if (rawPopularMovies.isNotEmpty()) {
            val injected = rawPopularMovies.injectAds(
                config = popularCarouselAdConfig.copy(sectionTag = "dashboard_popular_movies"),
                isAdsEnabled = adsEnabled
            )
            _uiState.update { it.copy(popularMovies = UiState.Success(injected)) }
        }

        if (rawPopularTvShows.isNotEmpty()) {
            val injected = rawPopularTvShows.injectAds(
                config = popularCarouselAdConfig.copy(sectionTag = "dashboard_popular_tv"),
                isAdsEnabled = adsEnabled
            )
            _uiState.update { it.copy(popularTvShows = UiState.Success(injected)) }
        }
    }

    init {
        viewModelScope.launch {
            billingRepository.isProActive.collect {
                applyAdInjection()
            }
        }

        viewModelScope.launch {
            reminderRepository.getActiveReminders().collectLatest { reminders ->
                _uiState.update { it.copy(activeReminders = reminders) }
            }
        }

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

        viewModelScope.launch {
            isTodayPollVotedUseCase().collect { isVoted ->
                _uiState.update { it.copy(isTodayPollVoted = isVoted) }
            }
        }

        fetchTrendingDiscussions()

        viewModelScope.launch {
            getCustomListsUseCase().collect { lists ->
                _uiState.update { it.copy(customLists = lists.take(10)) }
            }
        }

        viewModelScope.launch {
            appConfigRepository.notificationShelfLastDismissedMs.collect { dismissedEpoch ->
                val coolingDown = dismissedEpoch > 0L &&
                        System.currentTimeMillis() - dismissedEpoch < NOTIFICATION_SHELF_COOLDOWN_MS
                _uiState.update { it.copy(isNotificationShelfCoolingDown = coolingDown) }
            }
        }

        viewModelScope.launch {
            appConfigRepository.acknowledgedFeatures.collect { acknowledged ->
                _uiState.update { it.copy(acknowledgedFeatures = acknowledged) }
            }
        }

        viewModelScope.launch {
            combine(
                appConfigRepository.lastSeenWhatsNewCampaign,
                appConfigRepository.whatsNewCampaignId,
                appConfigRepository.isWhatsNewEnabled
            ) { lastSeen, currentCampaign, isEnabled ->
                val showBanner =
                    isEnabled && lastSeen != currentCampaign && lastSeen != "__UNINITIALIZED__"
                _uiState.update {
                    it.copy(
                        showWhatsNewBanner = showBanner,
                        currentCampaignId = currentCampaign
                    )
                }
            }.collect { }
        }
    }

    fun dismissWhatsNewBanner() {
        val currentCampaign = _uiState.value.currentCampaignId
        if (currentCampaign.isNotEmpty()) {
            viewModelScope.launch {
                appConfigRepository.updateLastSeenWhatsNewCampaign(currentCampaign)
            }
        }
    }

    private var hasLoadedCommunityLists = false

    fun setCuratedCommunitySelected(selected: Boolean) {
        _uiState.update { it.copy(isCuratedCommunitySelected = selected) }
        if (selected && !hasLoadedCommunityLists) {
            hasLoadedCommunityLists = true
            loadCommunityLists()
        }
    }

    private fun loadCommunityLists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCommunityListsLoading = true) }
            getCommunityListsUseCase(limit = 10).collect { lists ->
                val topLists = lists.take(10)
                _uiState.update { state ->
                    val updatedSelected = state.selectedCommunityListForDetail?.let { selected ->
                        topLists.find { it.listId == selected.listId } ?: selected
                    }
                    state.copy(
                        communityLists = topLists,
                        isCommunityListsLoading = false,
                        selectedCommunityListForDetail = updatedSelected
                    )
                }
            }
        }
    }

    fun selectCommunityListForDetail(list: CommunityCuratedList?) {
        _uiState.update { it.copy(selectedCommunityListForDetail = list) }
    }

    fun toggleCommunityListUpvote(listId: String) = viewModelScope.launch {
        toggleCommunityListUpvoteUseCase(ToggleListUpvoteParams(listId = listId))
    }

    fun cloneCommunityList(
        communityList: CommunityCuratedList,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) = viewModelScope.launch {
        when (cloneCommunityListUseCase(CloneCommunityListParams(communityList = communityList))) {
            is Result.Success -> onSuccess()
            is Result.Error -> onError()
        }
    }

    fun onFeatureTapped(feature: CinephileFeature) = viewModelScope.launch {
        appConfigRepository.acknowledgeFeature(feature.id)
    }

    private var dailyPollJob: Job? = null

    fun loadDailyPoll(forceRefresh: Boolean = false) {
        dailyPollJob?.cancel()
        dailyPollJob = viewModelScope.launch {
            _uiState.update { it.copy(isDailyPollLoading = true, dailyPollError = null) }
            getDailyPollUseCase(forceRefresh = forceRefresh)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isDailyPollLoading = false,
                            dailyPollError = throwable.localizedMessage
                        )
                    }
                }
                .collect { poll ->
                    _uiState.update {
                        it.copy(
                            dailyPoll = poll,
                            isTodayPollVoted = it.isTodayPollVoted || poll.hasVoted,
                            isDailyPollLoading = false,
                            dailyPollError = null
                        )
                    }
                }
        }
    }

    fun retryDailyPoll() {
        loadDailyPoll(forceRefresh = true)
    }

    fun voteDailyPoll(optionIndex: Int) {
        val currentPoll = _uiState.value.dailyPoll
        if (currentPoll.hasVoted) return

        // Instant 0ms Optimistic UI Update
        val newVoteCounts = if (currentPoll.voteCounts.isNotEmpty()) {
            currentPoll.voteCounts.toMutableList().also { counts ->
                if (optionIndex in counts.indices) {
                    counts[optionIndex] = counts[optionIndex] + 1
                }
            }.toList()
        } else {
            List(currentPoll.options.size) { if (it == optionIndex) 1 else 0 }
        }

        val optimisticPoll = currentPoll.copy(
            voteCounts = newVoteCounts,
            totalVotes = currentPoll.totalVotes + 1,
            selectedOptionIndex = optionIndex
        )

        _uiState.update {
            it.copy(
                dailyPoll = optimisticPoll,
                isTodayPollVoted = true,
                dailyPollError = null
            )
        }

        viewModelScope.launch {
            when (val result = voteDailyPollUseCase(optionIndex = optionIndex)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            dailyPoll = result.data,
                            isTodayPollVoted = true,
                            dailyPollError = null
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update { it.copy(dailyPollError = null) }
                }
            }
        }
    }

    fun fetchTrendingDiscussions(forceRefresh: Boolean = false) = viewModelScope.launch {
        getTrendingDiscussionsUseCase(forceRefresh = forceRefresh).collect { discussions ->
            _uiState.update { it.copy(trendingDiscussions = discussions) }
        }
    }

    fun fetchAllDashboardData() {
        fetchAllWatchProvidersUseCase.invalidateCache()
        movieGenresUseCase.invalidateCache()
        tvGenresUseCase.invalidateCache()
        fetchTrendingMedia()
        fetchPopularMovies()
        fetchPopularTvShows()
        fetchWatchProviders()
        fetchMovieGenres()
        fetchTvGenres()
        fetchTrendingDiscussions(forceRefresh = true)
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
            rawSpotlightItems = spotlightItems
            val injected = spotlightItems.injectAds(
                config = homeSpotlightAdConfig.copy(sectionTag = "dashboard_spotlight"),
                isAdsEnabled = isAdsCurrentlyEnabled()
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
                    val previews = movies.map { movie -> movie.asMoviePreview() }
                    rawPopularMovies = previews
                    previews.injectAds(
                        config = popularCarouselAdConfig.copy(sectionTag = "dashboard_popular_movies"),
                        isAdsEnabled = isAdsCurrentlyEnabled()
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
                    val previews = tvShows.map { tvShow -> tvShow.asTvShowPreview() }
                    rawPopularTvShows = previews
                    previews.injectAds(
                        config = popularCarouselAdConfig.copy(sectionTag = "dashboard_popular_tv"),
                        isAdsEnabled = isAdsCurrentlyEnabled()
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

    fun setMovieStudioSelected(selected: Boolean) {
        _uiState.update { it.copy(isMovieStudioSelected = selected) }
    }

    fun setMoviePopularSelected(selected: Boolean) {
        _uiState.update { it.copy(isMoviePopularSelected = selected) }
    }

    fun setMovieGenreSelected(selected: Boolean) {
        _uiState.update { it.copy(isMovieGenreSelected = selected) }
    }

    fun fetchMovieGenres() = viewModelScope.launch {
        _uiState.update { it.copy(movieGenres = UiState.Loading) }
        val result = movieGenresUseCase()
        _uiState.update { it.copy(movieGenres = result.asSuccessOrErrorUiState()) }
    }

    fun fetchTvGenres() = viewModelScope.launch {
        _uiState.update { it.copy(tvGenres = UiState.Loading) }
        val result = tvGenresUseCase()
        _uiState.update { it.copy(tvGenres = result.asSuccessOrErrorUiState()) }
    }

    fun openDailyPollSheet() {
        if (uiState.value.dailyPoll.question.isEmpty()) {
            loadDailyPoll()
        }
        _uiState.update { it.copy(showDailyPollSheet = true) }
    }

    fun dismissDailyPollSheet() {
        _uiState.update { it.copy(showDailyPollSheet = false) }
    }

    fun onNativeAdLoaded(nativeAd: NativeAd) {
        _uiState.update { it.copy(nativeAd = nativeAd, isNativeAdFailed = false) }
    }

    fun onNativeAdFailed() {
        _uiState.update { it.copy(isNativeAdFailed = true) }
    }

    fun onCarouselNativeAdLoaded(injectableAd: InjectableAd, nativeAd: NativeAd) {
        if (injectableAd.ad === nativeAd) return
        _uiState.update { currentState ->
            currentState.copy(
                trendingMedia = currentState.trendingMedia.updateNativeAd(injectableAd, nativeAd)
            )
        }
    }

    fun onCarouselNativeAdFailed(injectableAd: InjectableAd) {
        _uiState.update { currentState ->
            currentState.copy(
                trendingMedia = currentState.trendingMedia.removeNativeAd(injectableAd)
            )
        }
    }

    fun onPopularAdLoaded(injectableAd: InjectableAd, nativeAd: NativeAd) {
        if (injectableAd.ad === nativeAd) return
        _uiState.update { currentState ->
            currentState.copy(
                popularMovies = currentState.popularMovies.updateNativeAd(injectableAd, nativeAd),
                popularTvShows = currentState.popularTvShows.updateNativeAd(injectableAd, nativeAd)
            )
        }
    }

    fun onPopularAdFailed(injectableAd: InjectableAd) {
        _uiState.update { currentState ->
            currentState.copy(
                popularMovies = currentState.popularMovies.removeNativeAd(injectableAd),
                popularTvShows = currentState.popularTvShows.removeNativeAd(injectableAd)
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
                    completedShowDialog = CompletedShowDialogState(
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

    fun removeReminder(reminder: AiringReminder) = viewModelScope.launch {
        reminderRepository.removeReminder(reminder.mediaId, reminder.mediaType)
    }

    fun exportRemindersToIcs(context: Context) = viewModelScope.launch {
        if (!billingRepository.isProActive.value) {
            return@launch
        }
        val icsContent = reminderRepository.exportToIcs()
        if (icsContent.isNotBlank()) {
            try {
                val calendarDir = File(context.cacheDir, "shared_calendar").apply { mkdirs() }
                val icsFile = File(calendarDir, "showtime_reminders.ics")
                icsFile.writeText(icsContent)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    icsFile
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/calendar"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "ShowTime Airing Reminders")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(
                    Intent.createChooser(intent, "Export Airing Reminders").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            } catch (_: Exception) {
            }
        }
    }

    fun dismissNotificationShelf() = viewModelScope.launch {
        appConfigRepository.dismissNotificationShelf()
    }

    companion object {
        /** 7 days in milliseconds. */
        private const val NOTIFICATION_SHELF_COOLDOWN_MS = 7L * 24 * 60 * 60 * 1000
    }
}
