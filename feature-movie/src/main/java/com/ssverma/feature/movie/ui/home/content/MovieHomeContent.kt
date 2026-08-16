package com.ssverma.feature.movie.ui.home.content

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.notifications.LocalNotificationManager
import com.ssverma.core.ui.layout.rememberFloatingBottomBarPadding
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.openAppSettings
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.analytics.MovieAnalyticsEvent
import com.ssverma.feature.movie.analytics.MovieAnalyticsScreenName
import com.ssverma.feature.movie.analytics.MovieAnalyticsValues
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.ui.home.HomeMovieViewModel
import com.ssverma.feature.movie.ui.home.component.DiscoverySection
import com.ssverma.feature.movie.ui.home.component.HeroSection
import com.ssverma.feature.movie.ui.home.component.MovieGenres
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.AppSection
import com.ssverma.shared.ui.component.AttributionFooter
import com.ssverma.shared.ui.component.MediaListItemShimmer
import com.ssverma.shared.ui.component.NotificationPermissionBanner
import com.ssverma.shared.ui.component.WatchProviderHubSection
import com.ssverma.shared.ui.component.media.MovieListItem

@Composable
fun MovieHomeContent(
    viewModel: HomeMovieViewModel,
    openMovieList: (MovieListingArgs) -> Unit,
    openMovieDetails: (Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openWatchProviderHub: (ProviderInfo) -> Unit,
    openLibraryPage: () -> Unit,
    modifier: Modifier = Modifier
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
                // Permanently denied or user dismissed it twice, open settings
                context.openAppSettings()
            }
        }
    }


    LazyColumn(
        contentPadding = rememberFloatingBottomBarPadding(),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            HeroSection(
                trendingMoviesState = uiState.trendingMovies,
                onSearchClicked = openSearchPage,
                onAccountClicked = openAccountPage,
                openLibraryPage = openLibraryPage,
                onMovieClicked = { movie ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.MovieClicked(
                            movie = movie,
                            section = MovieAnalyticsValues.SECTION_TRENDING_CAROUSEL,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME,
                        )
                    )
                    openMovieDetails(movie.id)
                },
                onWatchProviderClick = openWatchProviderHub,
                onRetry = { viewModel.fetchTrendingMovies() },
                onAdLoaded = viewModel::onNativeAdLoaded
            )
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
                                // If we've already shown it and they said no, or if they permanently denied, go to settings
                                // But on first click, shouldShowRequestPermissionRationale is usually false.
                                // Actually, let's just launch it and handle the "nothing happened" via the result if we can.
                                // Or check if it's the first time.
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    },
                    onDismissClicked = { isPermissionBannerDismissed = true },
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium)
                )
            }
        }

        item {
            MovieGenres(
                genresUiState = uiState.genres,
                onGenreClicked = { genre ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.GenreClicked(
                            genre = genre,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME,
                        )
                    )
                    openMovieList(
                        MovieListingArgs.ByGenre(
                            title = genre.name,
                            genreId = genre.id
                        )
                    )
                },
                onRetry = { viewModel.fetchMovieGenres() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.large)
            )
        }

        item {
            WatchProviderHubSection(
                providersUiState = uiState.watchProviders,
                onProviderClick = { provider ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.WatchProviderClicked(
                            providerInfo = provider,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME
                        )
                    )
                    openWatchProviderHub(provider)
                },
                onRetry = { viewModel.fetchWatchProviders() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
            )
        }

        item {
            ShowTimeNativeAd(
                ad = uiState.feedInlineAd,
                loadInternally = uiState.feedInlineAd == null,
                onAdLoaded = viewModel::onFeedInlineAdLoaded,
                style = NativeAdStyle.List,
                analyticsEventPrefix = "movie_home_feed_inline_native",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.medium)
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }

        item {
            DiscoverySection(
                popularMoviesState = uiState.popularMovies,
                topRatedMoviesState = uiState.topRatedMovies,
                upcomingMoviesState = uiState.upcomingMovies,
                onMovieClicked = { moviePreview: MoviePreview ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.MovieClicked(
                            movie = moviePreview,
                            section = MovieAnalyticsValues.SECTION_DISCOVERY,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME,
                        )
                    )
                    openMovieDetails(moviePreview.id)
                },
                onSeeAllClicked = { args ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.SeeAllClicked(
                            section = MovieAnalyticsValues.SECTION_DISCOVERY,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME
                        )
                    )
                    openMovieList(args)
                },
                onFetchPopular = { viewModel.fetchPopularMovies() },
                onFetchTopRated = { viewModel.fetchTopRatedMovies() },
                onFetchUpcoming = { viewModel.fetchUpcomingMovies() },
                onWatchProviderClick = openWatchProviderHub,
                onAdLoaded = viewModel::onNativeAdLoaded,
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
            )
        }

        item {
            AppSection(
                title = stringResource(R.string.now_in_cinemas),
                uiState = uiState.inCinemasMovies,
                isVertical = true,
                onTrailingActionClicked = {
                    analytics.logEvent(
                        MovieAnalyticsEvent.SeeAllClicked(
                            section = MovieAnalyticsValues.SECTION_IN_CINEMAS,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME
                        )
                    )
                    openMovieList(
                        MovieListingArgs.NowInCinemas(
                            titleRes = R.string.now_in_cinemas
                        )
                    )
                },
                onRetry = { viewModel.fetchInCinemaMovies() },
                loadingPlaceholder = { MediaListItemShimmer() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                content = { injectableItem: AdInjectable<MoviePreview> ->
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
                            val moviePreview =
                                (injectableItem as InjectableContent<MoviePreview>).item
                            MovieListItem(
                                movie = moviePreview,
                                showRating = true,
                                onClick = {
                                    analytics.logEvent(
                                        MovieAnalyticsEvent.MovieClicked(
                                            movie = it,
                                            section = MovieAnalyticsValues.SECTION_IN_CINEMAS,
                                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME,
                                        )
                                    )
                                    openMovieDetails(it.id)
                                },
                                overlayContent = null,
                                indicator = {
                                    MovieIndicator(
                                        config = MovieListingConfig.Filterable.NowInCinemas(),
                                        movie = it
                                    )
                                },
                            )
                        }
                    }
                }
            )
        }


        item { AttributionFooter(modifier = Modifier.padding(top = MaterialTheme.spacing.large)) }
    }
}
