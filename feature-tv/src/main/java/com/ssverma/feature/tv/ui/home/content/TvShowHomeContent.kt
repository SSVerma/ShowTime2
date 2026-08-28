package com.ssverma.feature.tv.ui.home.content

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.notifications.LocalNotificationManager
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.scrim
import com.ssverma.core.ui.layout.rememberFloatingBarsPadding
import com.ssverma.core.ui.layout.rememberFloatingBottomBarHeight
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.openAppSettings
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.analytics.TvAnalyticsEvent
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.feature.tv.analytics.TvAnalyticsValues
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.ui.home.HomeTvShowViewModel
import com.ssverma.feature.tv.ui.home.component.DiscoverySection
import com.ssverma.feature.tv.ui.home.component.HeroSection
import com.ssverma.feature.tv.ui.home.component.TvGenres
import com.ssverma.feature.tv.ui.home.component.UpNextSection
import com.ssverma.feature.tv.ui.list.component.TvIndicator
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.AppSection
import com.ssverma.shared.ui.component.AttributionFooter
import com.ssverma.shared.ui.component.MediaListItemShimmer
import com.ssverma.shared.ui.component.NotificationPermissionBanner
import com.ssverma.shared.ui.component.WatchProviderHubSection
import com.ssverma.shared.ui.component.media.TvShowListItem

@Composable
fun TvShowHomeContent(
    viewModel: HomeTvShowViewModel,
    openTvShowList: (TvShowListingRoute) -> Unit,
    openTvShowDetails: (Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openWatchProviderHub: (ProviderInfo) -> Unit,
    openLibraryPage: (LibraryHomeNavKey) -> Unit,
    openTvSeasonDetails: (showTmdbId: Int, seasonNumber: Int) -> Unit = { id, _ ->
        openTvShowDetails(id)
    },
    modifier: Modifier = Modifier,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val analytics = LocalAnalytics.current

    val notificationManager = LocalNotificationManager.current
    var isPermissionBannerDismissed by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        mutableStateOf(notificationManager.hasNotificationPermission())
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = notificationManager.hasNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            val activity = context as? Activity
            if (activity != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                context.openAppSettings()
            }
        }
    }

    val lazyListState = rememberLazyListState()
    val bottomBarHeight = rememberFloatingBottomBarHeight()

    val trendingTvShows = (uiState.trendingTvShows as? UiState.Success)?.data.orEmpty()
    val carouselState = rememberCarouselState { trendingTvShows.size }

    Box(modifier = modifier.fillMaxSize()) {
        val currentBackdrop = remember(trendingTvShows, carouselState.currentItem) {
            val currentItem = trendingTvShows.getOrNull(carouselState.currentItem)
            val preview = (currentItem as? InjectableContent<*>)?.item as? TvShowPreview
            preview?.backdropImageUrl
        }

        currentBackdrop?.let { url ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(410.dp)
                    .graphicsLayer {
                        val firstIndex = lazyListState.firstVisibleItemIndex
                        val offset = lazyListState.firstVisibleItemScrollOffset
                        if (firstIndex == 0) {
                            translationY = -offset * 0.45f
                            alpha = (1f - (offset / 550f)).coerceIn(0f, 1f)
                        } else {
                            alpha = 0f
                        }
                    }
            ) {
                NetworkImage(
                    url = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .scrim(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.20f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.50f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                )
            }
        }

        LazyColumn(
            state = lazyListState,
            contentPadding = rememberFloatingBarsPadding(includeBottomBarPadding = false),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                HeroSection(
                    trendingTvShowsState = uiState.trendingTvShows,
                    carouselState = carouselState,
                    showBackdrop = false,
                    onSearchClicked = openSearchPage,
                    onAccountClicked = openAccountPage,
                    openLibraryPage = openLibraryPage,
                    onTvShowClicked = { tvShow ->
                        analytics.logEvent(
                            TvAnalyticsEvent.TvShowClicked(
                                tvShow = tvShow,
                                section = TvAnalyticsValues.SECTION_TRENDING_CAROUSEL,
                                sourceScreen = TvAnalyticsScreenName.TV_HOME
                            )
                        )
                        openTvShowDetails(tvShow.id)
                    },
                    onWatchProviderClick = openWatchProviderHub,
                    onRetry = { viewModel.fetchTrendingTvShows() },
                    onAdLoaded = viewModel::onNativeAdLoaded,
                    onShowFeedback = onShowFeedback
                )
            }

            item(key = "up_next_section") {
                AnimatedVisibility(
                    visible = uiState.upNextQueue.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    ),
                    exit = fadeOut(animationSpec = tween(250)) + shrinkVertically(
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                    )
                ) {
                    UpNextSection(
                        upNextEpisodes = uiState.upNextQueue,
                        onUpNextEpisodeClick = openTvSeasonDetails,
                        onMarkWatchedClick = { showTmdbId, season, episode ->
                            viewModel.markEpisodeWatched(showTmdbId, season, episode)
                        },
                        modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                    )
                }
            }

            if (!hasNotificationPermission && !isPermissionBannerDismissed) {
                item {
                    NotificationPermissionBanner(
                        onEnableClicked = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val activity = context as? Activity
                                if (activity != null &&
                                    !ActivityCompat.shouldShowRequestPermissionRationale(
                                        activity,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        },
                        onDismissClicked = { isPermissionBannerDismissed = true },
                        modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                    )
                }
            }

            item {
                TvGenres(
                    genresUiState = uiState.genres,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                    onGenreClicked = { genre ->
                        analytics.logEvent(
                            TvAnalyticsEvent.GenreClicked(
                                genre = genre,
                                sourceScreen = TvAnalyticsScreenName.TV_HOME
                            )
                        )
                        openTvShowList(
                            TvShowListingRoute(
                                args = TvShowListingArgs.ByGenre(
                                    genreId = genre.id,
                                    title = genre.name
                                )
                            )
                        )
                    },
                    onRetry = { viewModel.fetchTvGenres() }
                )
            }

            item {
                WatchProviderHubSection(
                    providersUiState = uiState.watchProviders,
                    onProviderClick = { provider ->
                        analytics.logEvent(
                            TvAnalyticsEvent.WatchProviderClicked(
                                providerInfo = provider,
                                sourceScreen = TvAnalyticsScreenName.TV_HOME
                            )
                        )
                        openWatchProviderHub(provider)
                    },
                    onRetry = { viewModel.fetchWatchProviders() },
                    isMovie = false,
                    source = "tv_home",
                    adContent = {
                        ShowTimeNativeAd(
                            ad = uiState.watchProviderAd,
                            loadInternally = uiState.watchProviderAd == null,
                            onAdLoaded = viewModel::onWatchProviderAdLoaded,
                            style = NativeAdStyle.CircularLogo,
                            modifier = Modifier.size(56.dp),
                            analyticsEventPrefix = "tv_home_watch_provider"
                        )
                    },
                    modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                )
            }

            item {
                ShowTimeNativeAd(
                    ad = uiState.feedInlineAd,
                    loadInternally = uiState.feedInlineAd == null,
                    onAdLoaded = viewModel::onFeedInlineAdLoaded,
                    style = NativeAdStyle.List,
                    analyticsEventPrefix = "tv_home_feed_inline_native",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.medium)
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )
            }

            item {
                DiscoverySection(
                    popularTvShowsState = uiState.popularTvShows,
                    topRatedTvShowsState = uiState.topRatedTvShows,
                    upcomingTvShowsState = uiState.upcomingTvShows,
                    onTvShowClicked = { tvShow ->
                        analytics.logEvent(
                            TvAnalyticsEvent.TvShowClicked(
                                tvShow = tvShow,
                                section = TvAnalyticsValues.SECTION_DISCOVERY,
                                sourceScreen = TvAnalyticsScreenName.TV_HOME
                            )
                        )
                        openTvShowDetails(tvShow.id)
                    },
                    onSeeAllClicked = { route ->
                        analytics.logEvent(
                            TvAnalyticsEvent.SeeAllClicked(
                                section = TvAnalyticsValues.SECTION_DISCOVERY,
                                sourceScreen = TvAnalyticsScreenName.TV_HOME
                            )
                        )
                        openTvShowList(route)
                    },
                    onFetchPopular = { viewModel.fetchPopularTvShows() },
                    onFetchTopRated = { viewModel.fetchTopRatedTvShows() },
                    onFetchUpcoming = { viewModel.fetchUpcomingTvShows() },
                    onWatchProviderClick = openWatchProviderHub,
                    onAdLoaded = viewModel::onNativeAdLoaded,
                    onShowFeedback = onShowFeedback,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                )
            }

            item {
                AppSection(
                    title = stringResource(R.string.airing_today),
                    leadingIcon = Icons.Rounded.CalendarToday,
                    leadingIconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.7f
                    ),
                    leadingIconTint = MaterialTheme.colorScheme.primary,
                    uiState = uiState.todayAiringTvShows,
                    isVertical = true,
                    onTrailingActionClicked = {
                        analytics.logEvent(
                            TvAnalyticsEvent.SeeAllClicked(
                                section = TvAnalyticsValues.SECTION_ON_THE_AIR,
                                sourceScreen = TvAnalyticsScreenName.TV_HOME
                            )
                        )
                        openTvShowList(
                            TvShowListingRoute(
                                args = TvShowListingArgs.AiringToday(titleRes = R.string.airing_today)
                            )
                        )
                    },
                    onRetry = { viewModel.fetchTodayAiringTvShows() },
                    loadingPlaceholder = { MediaListItemShimmer() },
                    modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                    content = { injectableItem: AdInjectable<TvShowPreview> ->
                        when (injectableItem) {
                            is InjectableAd -> {
                                ShowTimeNativeAd(
                                    ad = injectableItem.ad,
                                    onAdLoaded = { ad ->
                                        viewModel.onNativeAdLoaded(
                                            injectableItem,
                                            ad
                                        )
                                    },
                                    style = injectableItem.style
                                )
                            }

                            is InjectableContent<*> -> {
                                val tvShowPreview =
                                    (injectableItem as InjectableContent<TvShowPreview>).item
                                TvShowListItem(
                                    tvShow = tvShowPreview,
                                    showRating = true,
                                    overlayContent = {
                                        MediaStatsAction(
                                            mediaType = MediaType.Tv,
                                            mediaId = tvShowPreview.id,
                                            title = tvShowPreview.title,
                                            posterImageUrl = tvShowPreview.posterImageUrl,
                                            backdropImageUrl = tvShowPreview.backdropImageUrl,
                                            voteAvg = tvShowPreview.voteAvg,
                                            releaseDate = tvShowPreview.displayFirstAirDate.orEmpty(),
                                            containerColor = MaterialTheme.colorScheme.surface.copy(
                                                alpha = 0.85f
                                            ),
                                            onShowFeedback = onShowFeedback,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    },
                                    onClick = {
                                        analytics.logEvent(
                                            TvAnalyticsEvent.TvShowClicked(
                                                tvShow = it,
                                                section = TvAnalyticsValues.SECTION_ON_THE_AIR,
                                                sourceScreen = TvAnalyticsScreenName.TV_HOME
                                            )
                                        )
                                        openTvShowDetails(it.id)
                                    },
                                    indicator = {
                                        TvIndicator(
                                            config = TvShowListingConfig.Filterable.TodayAiring(),
                                            tvShow = it
                                        )
                                    },
                                )
                            }
                        }
                    }
                )
            }

            item {
                AppSection(
                    title = stringResource(R.string.now_airing),
                    leadingIcon = Icons.Rounded.LiveTv,
                    leadingIconContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                        alpha = 0.7f
                    ),
                    leadingIconTint = MaterialTheme.colorScheme.secondary,
                    uiState = uiState.nowAiringTvShows,
                    isVertical = true,
                    onTrailingActionClicked = {
                        analytics.logEvent(
                            TvAnalyticsEvent.SeeAllClicked(
                                section = TvAnalyticsValues.SECTION_ON_THE_AIR,
                                sourceScreen = TvAnalyticsScreenName.TV_HOME
                            )
                        )
                        openTvShowList(
                            TvShowListingRoute(
                                args = TvShowListingArgs.OnTheAir(titleRes = R.string.now_airing)
                            )
                        )
                    },
                    onRetry = { viewModel.fetchNowAiringTvShows() },
                    loadingPlaceholder = { MediaListItemShimmer() },
                    modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                    content = { injectableItem: AdInjectable<TvShowPreview> ->
                        when (injectableItem) {
                            is InjectableAd -> {
                                ShowTimeNativeAd(
                                    ad = injectableItem.ad,
                                    onAdLoaded = { ad ->
                                        viewModel.onNativeAdLoaded(
                                            injectableItem,
                                            ad
                                        )
                                    },
                                    style = injectableItem.style
                                )
                            }

                            is InjectableContent<*> -> {
                                val tvShowPreview =
                                    (injectableItem as InjectableContent<TvShowPreview>).item
                                TvShowListItem(
                                    tvShow = tvShowPreview,
                                    showRating = true,
                                    overlayContent = {
                                        MediaStatsAction(
                                            mediaType = MediaType.Tv,
                                            mediaId = tvShowPreview.id,
                                            title = tvShowPreview.title,
                                            posterImageUrl = tvShowPreview.posterImageUrl,
                                            backdropImageUrl = tvShowPreview.backdropImageUrl,
                                            voteAvg = tvShowPreview.voteAvg,
                                            releaseDate = tvShowPreview.displayFirstAirDate.orEmpty(),
                                            containerColor = MaterialTheme.colorScheme.surface.copy(
                                                alpha = 0.85f
                                            ),
                                            onShowFeedback = onShowFeedback,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    },
                                    onClick = {
                                        analytics.logEvent(
                                            TvAnalyticsEvent.TvShowClicked(
                                                tvShow = it,
                                                section = TvAnalyticsValues.SECTION_ON_THE_AIR,
                                                sourceScreen = TvAnalyticsScreenName.TV_HOME
                                            )
                                        )
                                        openTvShowDetails(it.id)
                                    },
                                    indicator = {
                                        TvIndicator(
                                            config = TvShowListingConfig.Filterable.NowAiring(),
                                            tvShow = it
                                        )
                                    },
                                )
                            }
                        }
                    }
                )
            }


            item {
                AttributionFooter(
                    bottomPadding = bottomBarHeight,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.large)
                )
            }
        }

        uiState.completedShowDialog?.let { dialogState ->
            com.ssverma.shared.ui.component.SeasonCompletionDialog(
                state = dialogState,
                onDismiss = viewModel::dismissCompletedShowDialog
            )
        }
    }
}
