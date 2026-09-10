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
import androidx.compose.ui.platform.LocalContext
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
import com.ssverma.feature.library.navigation.LibraryTabDestination
import com.ssverma.feature.tv.ui.home.component.UpNextSection
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.community.DiscussionNavArgs
import com.ssverma.shared.ui.component.AttributionFooter
import com.ssverma.shared.ui.component.SeasonCompletionDialog
import com.ssverma.shared.ui.component.section.ActiveRemindersSection
import com.ssverma.showtime.feature.filter.navigation.UniversalDiscoveryNavKey
import com.ssverma.showtime.ui.dashboard.shelves.DailyPollBottomSheet
import com.ssverma.showtime.ui.dashboard.shelves.StudioPortalItem
import com.ssverma.showtime.ui.dashboard.shelves.cinephileQuickAccessHub
import com.ssverma.showtime.ui.dashboard.shelves.dashboardGenreShelf
import com.ssverma.showtime.ui.dashboard.shelves.inViewportNativeAdShelf
import com.ssverma.showtime.ui.dashboard.shelves.notificationPermissionShelf
import com.ssverma.showtime.ui.dashboard.shelves.rateShowTimeShelf
import com.ssverma.showtime.ui.dashboard.shelves.streamingUniverseShelf
import com.ssverma.showtime.ui.dashboard.shelves.studioPortalsShelf
import com.ssverma.showtime.ui.dashboard.shelves.trendingDiscussionsShelf
import com.ssverma.showtime.ui.dashboard.shelves.trendingSpotlightShelf
import com.ssverma.showtime.ui.dashboard.shelves.trendingWorldwideShelf
import com.ssverma.showtime.ui.dashboard.shelves.universalDiscoveryShelf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    openMovieDetails: (Int) -> Unit,
    openTvShowDetails: (Int) -> Unit,
    openDiscussions: (DiscussionNavArgs) -> Unit,
    openCinemaGame: () -> Unit,
    openWatchProviderHub: (ProviderInfo, Boolean) -> Unit,
    openStudioPortal: (StudioPortalItem) -> Unit,
    openMovieListing: () -> Unit,
    openTvListing: () -> Unit,
    openLibraryPage: (LibraryHomeNavKey) -> Unit,
    openUniversalDiscovery: (UniversalDiscoveryNavKey) -> Unit,
    openCinemaDiary: () -> Unit = {},
    openTasteProfile: () -> Unit = {},
    openWrapped: () -> Unit = {},
    openBacklogChallenges: () -> Unit = {},
    openReceipt: () -> Unit = {},
    openPeople: () -> Unit = {},
    openMovieMatch: () -> Unit = {},
    openMovieGenreListing: (Genre) -> Unit = {},
    openTvGenreListing: (Genre) -> Unit = {},
    openProPaywall: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    TrackScreenView(screenName = "dashboard_home")

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
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
                    onShowFeedback = { args ->
                        coroutineScope.launch {
                            val result = snackbarHostState.showImmediateSnackbar(
                                message = args.message,
                                actionLabel = args.actionLabel,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                openLibraryPage(args.destination ?: LibraryHomeNavKey.Default)
                            }
                        }
                    }
                )

                // 2. Cinephile Quick Access Hub (All drawer & list entry points)
                cinephileQuickAccessHub(
                    gameStats = uiState.gameStats,
                    isTodayGameCompleted = uiState.isTodayGameCompleted,
                    isPollVoted = uiState.dailyPoll.hasVoted,
                    onOpenMyLists = {
                        openLibraryPage(LibraryHomeNavKey(initialTab = LibraryTabDestination.CustomLists))
                    },
                    onOpenCommunityLists = {
                        openLibraryPage(LibraryHomeNavKey(initialTab = LibraryTabDestination.Community))
                    },
                    onOpenCinemaDiary = openCinemaDiary,
                    onOpenCinemaGame = openCinemaGame,
                    onOpenDailyPoll = viewModel::openDailyPollSheet,
                    onOpenTasteProfile = openTasteProfile,
                    onOpenBacklogChallenges = openBacklogChallenges,
                    onOpenReceipt = openReceipt,
                    onOpenPeople = openPeople,
                    onOpenMovieMatch = openMovieMatch,
                    onOpenDiscovery = { openUniversalDiscovery(UniversalDiscoveryNavKey()) }
                )

                // 3. In-Viewport Native Ad Showcase (Guaranteed initial viewport viewability & high CPM)
                inViewportNativeAdShelf(
                    nativeAd = uiState.nativeAd,
                    onAdLoaded = viewModel::onNativeAdLoaded
                )

                // 4. Notification Permission Shelf (Conditional, Android 13+)
                notificationPermissionShelf()

                // 5. Up Next to Watch (Personalized continue watching queue)
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

                // 5b. Airing Reminders Shelf (Episodes & releases with calendar sync)
                item(key = "dashboard_airing_reminders_section") {
                    AnimatedVisibility(
                        visible = uiState.activeReminders.isNotEmpty(),
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
                        ActiveRemindersSection(
                            reminders = uiState.activeReminders,
                            onReminderClick = { reminder ->
                                when (reminder.mediaType) {
                                    MediaType.Tv -> openTvShowDetails(reminder.mediaId)
                                    else -> openMovieDetails(reminder.mediaId)
                                }
                            },
                            onRemoveReminderClick = viewModel::removeReminder,
                            onExportCalendarClick = {
                                if (isPro) {
                                    viewModel.exportRemindersToIcs(context)
                                } else {
                                    openProPaywall()
                                }
                            },
                            modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                        )
                    }
                }

                // 6. Universal Discovery & Browse Hub ("What to Watch Tonight")
                universalDiscoveryShelf(
                    onOpenDiscovery = openUniversalDiscovery
                )

                // 7. Explore Genres (Segmented: [ 🎬 Movies | 📺 TV Shows ])
                dashboardGenreShelf(
                    movieGenres = uiState.movieGenres,
                    tvGenres = uiState.tvGenres,
                    isMovieSelected = uiState.isMovieGenreSelected,
                    onToggleGenreType = viewModel::setMovieGenreSelected,
                    onGenreClicked = { genre, isMovie ->
                        if (isMovie) {
                            openMovieGenreListing(genre)
                        } else {
                            openTvGenreListing(genre)
                        }
                    },
                    onRetry = {
                        if (uiState.isMovieGenreSelected) {
                            viewModel.fetchMovieGenres()
                        } else {
                            viewModel.fetchTvGenres()
                        }
                    }
                )

                // 8. Popular Media Worldwide Shelf (Segmented: [ 🎬 Movies | 📺 TV Shows ])
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
                    onShowFeedback = { args ->
                        coroutineScope.launch {
                            val result = snackbarHostState.showImmediateSnackbar(
                                message = args.message,
                                actionLabel = args.actionLabel,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                openLibraryPage(args.destination ?: LibraryHomeNavKey.Default)
                            }
                        }
                    }
                )

                // 9. Streaming Universe Hub (Segmented: [ 🎬 Movies | 📺 TV Shows ])
                streamingUniverseShelf(
                    movieProviders = uiState.movieProviders,
                    tvProviders = uiState.tvProviders,
                    isMovieSelected = uiState.isMovieStreamingSelected,
                    onToggleStreamingType = viewModel::setMovieStreamingSelected,
                    onProviderClick = openWatchProviderHub,
                    onRetry = { viewModel.fetchWatchProviders() }
                )

                // 10. Cinephile Studio & Network Hubs (A24, HBO, Studio Ghibli, Pixar)
                studioPortalsShelf(
                    isMovieSelected = uiState.isMovieStudioSelected,
                    onToggleStudioType = viewModel::setMovieStudioSelected,
                    onPortalClick = openStudioPortal
                )

                // 11. Trending Community Discussions & Cinephile Buzz
                trendingDiscussionsShelf(
                    discussions = uiState.trendingDiscussions,
                    onDiscussionClick = openDiscussions
                )

                // 12. Rate Us & Share Showcase
                rateShowTimeShelf()

                // 13. TMDB Attribution Footer (Edge-to-edge till bottom end)
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
                SeasonCompletionDialog(
                    state = dialogState,
                    onDismiss = viewModel::dismissCompletedShowDialog
                )
            }

            if (uiState.showDailyPollSheet) {
                DailyPollBottomSheet(
                    poll = uiState.dailyPoll,
                    onOptionClick = viewModel::voteDailyPoll,
                    onDismiss = viewModel::dismissDailyPollSheet
                )
            }
        }
    }
}
