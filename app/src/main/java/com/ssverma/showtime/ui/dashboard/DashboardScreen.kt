package com.ssverma.showtime.ui.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.component.scrim
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.layout.rememberFloatingBarsPadding
import com.ssverma.core.ui.layout.rememberFloatingBottomBarHeight
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.tv.ui.home.component.UpNextSection
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.ui.component.AttributionFooter
import com.ssverma.showtime.ui.dashboard.shelves.StudioPortalItem
import com.ssverma.showtime.ui.dashboard.shelves.dailyHabitShelf
import com.ssverma.showtime.ui.dashboard.shelves.dailyPollShelf
import com.ssverma.showtime.ui.dashboard.shelves.inViewportNativeAdShelf
import com.ssverma.showtime.ui.dashboard.shelves.streamingUniverseShelf
import com.ssverma.showtime.ui.dashboard.shelves.studioPortalsShelf
import com.ssverma.showtime.ui.dashboard.shelves.trendingSpotlightShelf
import com.ssverma.showtime.ui.dashboard.shelves.trendingWorldwideShelf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    openMovieDetails: (Int) -> Unit,
    openTvShowDetails: (Int) -> Unit,
    openCinemaGame: () -> Unit,
    openWatchProviderHub: (ProviderInfo, Boolean) -> Unit,
    openStudioPortal: (StudioPortalItem) -> Unit,
    openMovieListing: () -> Unit,
    openTvListing: () -> Unit,
    openLibraryPage: (LibraryHomeNavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    TrackScreenView(screenName = "dashboard_home")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val bottomBarHeight = rememberFloatingBottomBarHeight()
    val lazyListState = rememberLazyListState()

    val trendingMedia = (uiState.trendingMedia as? UiState.Success)?.data.orEmpty()
    val carouselState = rememberCarouselState { trendingMedia.size }

    val currentBackdrop = remember(trendingMedia, carouselState.currentItem) {
        val currentItem = trendingMedia.getOrNull(carouselState.currentItem)
        if (currentItem is InjectableContent<*>) {
            (currentItem as InjectableContent<TrendingSpotlightItem>).item.backdropImageUrl
        } else {
            null
        }
    }

    val scrimColor = MaterialTheme.colorScheme.background

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Edge-to-edge Backdrop extending from the Status Bar down below the Spotlight Carousel
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
                // 1. Top Cinematic Trending Spotlight (TMDB /trending/all/day with mixed Movies & TV Shows)
                trendingSpotlightShelf(
                    trendingState = uiState.trendingMedia,
                    carouselState = carouselState,
                    onMovieClick = { openMovieDetails(it.id) },
                    onTvShowClick = { openTvShowDetails(it.id) },
                    onAdLoaded = viewModel::onCarouselNativeAdLoaded,
                    onRetry = { viewModel.fetchTrendingMedia() },
                    onShowFeedback = { message, actionLabel, destination ->
                        coroutineScope.launch {
                            val result = snackbarHostState.showImmediateSnackbar(
                                message = message,
                                actionLabel = actionLabel,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                openLibraryPage(destination ?: LibraryHomeNavKey.Default)
                            }
                        }
                    }
                )

                // 2. Up Next to Watch (Personalized continue watching queue)
                item(key = "dashboard_up_next_section") {
                    AnimatedVisibility(
                        visible = uiState.upNextQueue.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ),
                        exit = fadeOut(animationSpec = tween(250)) + shrinkVertically(
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutSlowInEasing
                            )
                        )
                    ) {
                        UpNextSection(
                            upNextEpisodes = uiState.upNextQueue,
                            onUpNextEpisodeClick = { showTmdbId, _ ->
                                openTvShowDetails(showTmdbId)
                            },
                            onMarkWatchedClick = { showTmdbId, seasonNumber, episodeNumber ->
                                viewModel.markEpisodeWatched(
                                    showTmdbId = showTmdbId,
                                    season = seasonNumber,
                                    episode = episodeNumber
                                )
                            },
                            modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                        )
                    }
                }

                // 3. Compact Daily Habit Hub (Cinema Challenge #690 & Streak)
                dailyHabitShelf(
                    gameStats = uiState.gameStats,
                    isTodayCompleted = uiState.isTodayGameCompleted,
                    onOpenGame = openCinemaGame
                )

                // 3. In-Viewport Native Ad Showcase (Guaranteed viewability & high CPM)
                inViewportNativeAdShelf(
                    nativeAd = uiState.nativeAd,
                    onAdLoaded = viewModel::onNativeAdLoaded
                )

                // 4. Daily Community Cinema Poll & Debate
                dailyPollShelf(
                    poll = uiState.dailyPoll,
                    onOptionClick = viewModel::voteDailyPoll
                )

                // 4. Streaming Universe Hub (Segmented: [ 🎬 Movies | 📺 TV Shows ])
                streamingUniverseShelf(
                    movieProviders = uiState.movieProviders,
                    tvProviders = uiState.tvProviders,
                    isMovieSelected = uiState.isMovieStreamingSelected,
                    onToggleStreamingType = viewModel::setMovieStreamingSelected,
                    onProviderClick = openWatchProviderHub,
                    onRetry = { viewModel.fetchWatchProviders() }
                )

                // 5. Cinephile Studio & Network Hubs (A24, HBO, Studio Ghibli, Pixar)
                studioPortalsShelf(
                    onPortalClick = openStudioPortal
                )

                // 6. Popular Media Worldwide Shelf (Segmented: [ 🎬 Movies | 📺 TV Shows ])
                trendingWorldwideShelf(
                    isMoviePopularSelected = uiState.isMoviePopularSelected,
                    popularMoviesState = uiState.popularMovies,
                    popularTvShowsState = uiState.popularTvShows,
                    onTogglePopularType = viewModel::setMoviePopularSelected,
                    onMovieClick = { openMovieDetails(it.id) },
                    onTvShowClick = { openTvShowDetails(it.id) },
                    onSeeAllClick = {
                        if (uiState.isMoviePopularSelected) {
                            openMovieListing()
                        } else {
                            openTvListing()
                        }
                    },
                    onAdLoaded = viewModel::onPopularAdLoaded,
                    onRetry = {
                        if (uiState.isMoviePopularSelected) {
                            viewModel.fetchPopularMovies()
                        } else {
                            viewModel.fetchPopularTvShows()
                        }
                    },
                    onShowFeedback = { message, actionLabel, destination ->
                        coroutineScope.launch {
                            val result = snackbarHostState.showImmediateSnackbar(
                                message = message,
                                actionLabel = actionLabel,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                openLibraryPage(destination ?: LibraryHomeNavKey.Default)
                            }
                        }
                    }
                )

                // 7. TMDB Attribution Footer (Edge-to-edge till bottom end)
                item(key = "dashboard_tmdb_attribution") {
                    AttributionFooter(
                        bottomPadding = bottomBarHeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp)
                    )
                }
            }

            // Snackbar Host with floating bottom bar offset
            ShowTimeSnackbarHost(
                hostState = snackbarHostState,
                floatingBottomBar = true,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            uiState.completedShowDialog?.let { dialogState ->
                com.ssverma.shared.ui.component.SeasonCompletionDialog(
                    state = dialogState,
                    onDismiss = viewModel::dismissCompletedShowDialog
                )
            }
        }
    }
}
