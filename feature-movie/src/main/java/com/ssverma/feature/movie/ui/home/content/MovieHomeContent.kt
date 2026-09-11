package com.ssverma.feature.movie.ui.home.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Theaters
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.scrim
import com.ssverma.core.ui.layout.rememberFloatingBarsPadding
import com.ssverma.core.ui.layout.rememberFloatingBottomBarHeight
import com.ssverma.core.ui.theme.spacing
import androidx.navigation3.runtime.NavKey
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
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
import com.ssverma.shared.ui.component.WatchProviderHubSection
import com.ssverma.shared.ui.component.media.ShowFeedbackArgs
import com.ssverma.shared.ui.component.media.UniversalMediaCard
import com.ssverma.shared.ui.component.media.asUniversalMediaItem

@Composable
fun MovieHomeContent(
    viewModel: HomeMovieViewModel,
    openMovieList: (MovieListingArgs) -> Unit,
    openMovieDetails: (Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openWatchProviderHub: (ProviderInfo) -> Unit,
    openLibraryPage: (NavKey) -> Unit,
    openCinemaGame: () -> Unit,
    modifier: Modifier = Modifier,
    onShowFeedback: ((ShowFeedbackArgs) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val analytics = LocalAnalytics.current

    val bottomBarHeight = rememberFloatingBottomBarHeight()
    val lazyListState = rememberLazyListState()

    val trendingMovies = (uiState.trendingMovies as? UiState.Success)?.data.orEmpty()
    val carouselState = rememberCarouselState { trendingMovies.size }

    val currentBackdrop = remember(trendingMovies, carouselState.currentItem) {
        val currentItem = trendingMovies.getOrNull(carouselState.currentItem)
        if (currentItem is InjectableContent<*>) {
            (currentItem as InjectableContent<MoviePreview>).item.backdropImageUrl
        } else {
            null
        }
    }

    val scrimColor = MaterialTheme.colorScheme.background

    Box(modifier = modifier.fillMaxSize()) {
        // Edge-to-edge Backdrop extending from the Status Bar down below the Hero Carousel
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
                                scrimColor.copy(alpha = 0.20f),
                                scrimColor.copy(alpha = 0.50f),
                                scrimColor.copy(alpha = 0.85f),
                                scrimColor
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
                    trendingMoviesState = uiState.trendingMovies,
                    carouselState = carouselState,
                    showBackdrop = false,
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
                    onAdLoaded = viewModel::onNativeAdLoaded,
                    onShowFeedback = onShowFeedback
                )
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
                    isMovie = true,
                    source = "movie_home",
                    adContent = {
                        ShowTimeNativeAd(
                            ad = uiState.watchProviderAd,
                            loadInternally = uiState.watchProviderAd == null,
                            onAdLoaded = viewModel::onWatchProviderAdLoaded,
                            style = NativeAdStyle.CircularLogo,
                            modifier = Modifier.size(56.dp),
                            analyticsEventPrefix = "movie_home_watch_provider"
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
                    onShowFeedback = onShowFeedback,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                )
            }

            item {
                AppSection(
                    title = stringResource(R.string.now_in_cinemas),
                    leadingIcon = Icons.Rounded.Theaters,
                    leadingIconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.7f
                    ),
                    leadingIconTint = MaterialTheme.colorScheme.primary,
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
                                UniversalMediaCard(
                                    item = moviePreview.asUniversalMediaItem(),
                                    onClick = {
                                        analytics.logEvent(
                                            MovieAnalyticsEvent.MovieClicked(
                                                movie = moviePreview,
                                                section = MovieAnalyticsValues.SECTION_IN_CINEMAS,
                                                sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME,
                                            )
                                        )
                                        openMovieDetails(moviePreview.id)
                                    },
                                    isGridView = false,
                                    topStartSlot = {
                                        MovieIndicator(
                                            config = MovieListingConfig.Filterable.NowInCinemas(),
                                            movie = moviePreview
                                        )
                                    },
                                    onShowFeedback = onShowFeedback,
                                    modifier = Modifier.fillMaxWidth()
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
    }
}
