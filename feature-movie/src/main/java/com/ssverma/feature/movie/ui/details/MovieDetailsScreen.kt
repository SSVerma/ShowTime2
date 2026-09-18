package com.ssverma.feature.movie.ui.details

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.ads.ui.rememberNativeAd
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.library.navigation.CinemaDiaryNavKey
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.analytics.MovieAnalyticsEvent
import com.ssverma.feature.movie.analytics.MovieAnalyticsScreenName
import com.ssverma.feature.movie.analytics.MovieAnalyticsValues
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.ui.details.component.MovieDetailsOverlays
import com.ssverma.feature.movie.ui.details.component.movieCollectionSection
import com.ssverma.feature.movie.ui.details.component.movieCreditsSection
import com.ssverma.feature.movie.ui.details.component.movieDetailsHeroSection
import com.ssverma.feature.movie.ui.details.component.movieDiscussionsSection
import com.ssverma.feature.movie.ui.details.component.movieGenresSection
import com.ssverma.feature.movie.ui.details.component.movieImageShotsSection
import com.ssverma.feature.movie.ui.details.component.movieKeywordsSection
import com.ssverma.feature.movie.ui.details.component.movieOverviewSection
import com.ssverma.feature.movie.ui.details.component.movieReactionsSection
import com.ssverma.feature.movie.ui.details.component.movieRecommendationsSection
import com.ssverma.feature.movie.ui.details.component.movieReviewsSection
import com.ssverma.feature.movie.ui.details.component.movieSimilarMoviesSection
import com.ssverma.feature.movie.ui.details.component.movieVideoShotsSection
import com.ssverma.feature.movie.ui.details.component.movieWatchProvidersSection
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.community.DiscussionNavArgs
import com.ssverma.shared.domain.utils.ShareMediaUtils
import com.ssverma.shared.ui.component.media.ShowFeedbackArgs
import com.ssverma.shared.ui.component.notification.rememberNotificationPermissionHandler
import com.ssverma.shared.ui.component.section.topSectionSpacing
import kotlinx.coroutines.launch
import com.ssverma.shared.ui.R as SharedR

@Composable
fun MovieDetailsScreen(
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: (movieId: Int) -> Unit,
    openDiscussionsList: (DiscussionNavArgs) -> Unit = {},
    openPersonDetails: (Cast) -> Unit,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
    openWatchHub: (providerInfo: ProviderInfo) -> Unit,
    openLibraryPage: (NavKey) -> Unit = {},
    openProPaywall: () -> Unit = {},
    viewModel: MovieDetailsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrackScreenView(screenName = MovieAnalyticsScreenName.MOVIE_DETAILS)

    Surface(color = MaterialTheme.colorScheme.background) {
        DriveCompose(
            uiState = uiState,
            onRetry = { viewModel.fetchMovieDetails() }
        ) { data ->
            MovieContent(
                data = data,
                viewModel = viewModel,
                onBackPressed = onBackPressed,
                openMovieDetails = openMovieDetails,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                openReviewsList = { openReviewsList(data.movie.id) },
                openDiscussionsList = {
                    openDiscussionsList(
                        DiscussionNavArgs(
                            mediaType = MediaType.Movie,
                            mediaId = data.movie.id,
                            title = data.movie.title,
                            posterImageUrl = data.movie.posterImageUrl,
                            backdropImageUrl = data.movie.backdropImageUrl
                        )
                    )
                },
                openYoutube = { videoId ->
                    viewModel.openYoutubeApp(videoId = videoId)
                },
                openPersonDetails = openPersonDetails,
                openMovieList = openMovieList,
                openWatchHub = openWatchHub,
                openLibraryPage = openLibraryPage,
                openProPaywall = openProPaywall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieContent(
    data: MovieDetailsData,
    viewModel: MovieDetailsViewModel,
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: () -> Unit,
    openDiscussionsList: () -> Unit,
    openYoutube: (videoId: String) -> Unit,
    openPersonDetails: (Cast) -> Unit,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
    openWatchHub: (providerInfo: ProviderInfo) -> Unit,
    openLibraryPage: (NavKey) -> Unit = {},
    openProPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val movie = data.movie
    val context = LocalContext.current
    val watchProviderRegion by viewModel.watchProviderRegion.collectAsStateWithLifecycle()
    val selectedProviderPayload by viewModel.selectedProviderForAction.collectAsStateWithLifecycle()
    val mediaReactions by viewModel.mediaReactions.collectAsStateWithLifecycle()
    val discussions by viewModel.discussions.collectAsStateWithLifecycle()
    val diaryEntries by viewModel.diaryEntries.collectAsStateWithLifecycle()
    val hasReminder by viewModel.hasReminder.collectAsStateWithLifecycle()
    val isReminderSheetVisible by viewModel.isReminderSheetVisible.collectAsStateWithLifecycle()
    val reminderLeadDays by viewModel.reminderLeadDays.collectAsStateWithLifecycle()
    val reminderNotificationHour by viewModel.reminderNotificationHour.collectAsStateWithLifecycle()
    val reminderNotificationMinute by viewModel.reminderNotificationMinute.collectAsStateWithLifecycle()
    val activeGateConfig by viewModel.activeGateConfig.collectAsStateWithLifecycle()
    val isAdLoading by viewModel.isAdLoading.collectAsStateWithLifecycle()
    val reminderSnackbarEvent by viewModel.reminderSnackbarEvent.collectAsStateWithLifecycle()
    val movieCollection by viewModel.movieCollection.collectAsStateWithLifecycle()
    val isCollectionLoading by viewModel.isCollectionLoading.collectAsStateWithLifecycle()
    var showLogDialog by remember { mutableStateOf(false) }
    val analytics = LocalAnalytics.current
    val watchProviderAd = rememberNativeAd(analyticsEventPrefix = "movie_details_watch_provider")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val notificationPermissionHandler = rememberNotificationPermissionHandler()

    val handleShowFeedback: (ShowFeedbackArgs) -> Unit = { args ->
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

    LaunchedEffect(reminderSnackbarEvent) {
        reminderSnackbarEvent?.let { message ->
            snackbarHostState.showImmediateSnackbar(message)
            viewModel.clearReminderSnackbarEvent()
        }
    }

    val diaryUpdatedMessage = stringResource(
        SharedR.string.media_menu_diary_updated_success,
        movie.title
    )
    val diaryLoggedMessage = stringResource(
        SharedR.string.media_menu_diary_logged_success,
        movie.title
    )
    val viewInDiaryLabel = stringResource(SharedR.string.media_menu_view_in_diary)
    val horizontalSpacing = MaterialTheme.spacing.medium

    Scaffold(
        snackbarHost = { ShowTimeSnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            movieDetailsHeroSection(
                movie = movie,
                hasReminder = hasReminder,
                existingDiaryEntry = diaryEntries.firstOrNull(),
                hasDiaryEntries = diaryEntries.isNotEmpty(),
                onBackPressed = onBackPressed,
                onPlayTrailer = {
                    analytics.logEvent(
                        MovieAnalyticsEvent.TrailerClicked(
                            movieId = movie.id,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    viewModel.onPlayTrailerClicked(movie)
                },
                onOpenLogDialog = { showLogDialog = true },
                onReminderClick = {
                    if (hasReminder) {
                        viewModel.openReminderSheet()
                    } else {
                        notificationPermissionHandler.requestPermissionThen {
                            viewModel.openReminderSheet()
                        }
                    }
                },
                onOpenDiscussions = {
                    openDiscussionsList()
                },
                onShare = {
                    analytics.logEvent(
                        MovieAnalyticsEvent.ShareClicked(
                            movieId = movie.id,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    val shareableText = ShareMediaUtils.buildShareableMediaText(
                        mediaTitle = movie.title,
                        mediaTagline = movie.tagline,
                        mediaOverview = movie.overview,
                        appPackageName = context.packageName,
                        mediaType = "movie",
                        mediaId = movie.id
                    )
                    context.dispatchShareTextIntent(text = shareableText)
                },
                onShowFeedback = handleShowFeedback
            )

            movieWatchProvidersSection(
                movie = movie,
                watchProviderRegion = watchProviderRegion,
                watchProviderAd = watchProviderAd,
                onWatchProviderClick = { providerInfo ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.WatchProviderClicked(
                            providerInfo = providerInfo,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openWatchHub(providerInfo)
                },
                onWatchProviderWithCategoryClick = { providerInfo, category ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.WatchProviderClicked(
                            providerInfo = providerInfo,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    viewModel.onProviderSelectedForAction(providerInfo, category)
                },
                onJustWatchClick = {
                    analytics.logEvent(
                        MovieAnalyticsEvent.JustWatchClicked(
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                },
                modifier = Modifier.topSectionSpacing()
            )

            movieReactionsSection(
                mediaReactions = mediaReactions,
                onReactionTagClicked = viewModel::onReactionTagClicked,
                modifier = Modifier.topSectionSpacing()
            )

            movieOverviewSection(
                overview = movie.overview,
                modifier = Modifier
                    .topSectionSpacing()
                    .padding(horizontal = horizontalSpacing)
            )

            movieGenresSection(
                genres = movie.generes,
                onGenreClicked = { genre ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.GenreClicked(
                            genre = genre,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openMovieList(
                        MovieListingArgs.ByGenre(
                            title = genre.name,
                            genreId = genre.id
                        )
                    )
                },
                modifier = Modifier.topSectionSpacing()
            )

            movieCreditsSection(
                casts = movie.casts,
                onPersonClick = { cast ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.CastClicked(
                            cast = cast,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openPersonDetails(cast)
                },
                modifier = Modifier.topSectionSpacing()
            )

            movieImageShotsSection(
                imageShots = data.imageShots,
                openImageShotsList = {
                    analytics.logEvent(
                        MovieAnalyticsEvent.SeeAllClicked(
                            section = MovieAnalyticsValues.SECTION_SHOTS,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openImageShotsList()
                },
                openImageShot = { index ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.ImageShotClicked(
                            index = index,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openImageShot(index)
                },
                modifier = Modifier.topSectionSpacing()
            )

            movieVideoShotsSection(
                videos = movie.videos,
                onVideoClick = { video ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.VideoClicked(
                            video = video,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openYoutube(video.key)
                },
                modifier = Modifier.topSectionSpacing()
            )

            movieDiscussionsSection(
                discussions = discussions,
                onDiscussionsViewAllClick = openDiscussionsList,
                onPostComment = viewModel::postComment,
                onEditComment = viewModel::editComment,
                onReportComment = viewModel::reportComment,
                onToggleUpvote = viewModel::toggleCommentUpvote,
                onDeleteComment = viewModel::deleteComment,
                modifier = Modifier.topSectionSpacing()
            )

            movieReviewsSection(
                reviews = movie.reviews,
                onReviewsViewAllClick = {
                    analytics.logEvent(
                        MovieAnalyticsEvent.SeeAllClicked(
                            section = MovieAnalyticsValues.SECTION_REVIEWS,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openReviewsList()
                },
                onReviewClick = {
                    analytics.logEvent(
                        MovieAnalyticsEvent.ReviewClicked(
                            review = it,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                },
                modifier = Modifier.topSectionSpacing()
            )

            val effectiveCollection = movieCollection ?: movie.movieCollection
            movieCollectionSection(
                movieCollection = effectiveCollection,
                currentMovieId = movie.id,
                isLoadingParts = isCollectionLoading,
                onMovieClick = { collectionMovieId ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.MovieClicked(
                            movieId = collectionMovieId,
                            movieTitle = "",
                            section = MovieAnalyticsValues.SECTION_COLLECTION,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openMovieDetails(collectionMovieId)
                },
                modifier = Modifier.topSectionSpacing()
            )

            movieSimilarMoviesSection(
                movies = movie.similarMovies,
                onMovieClick = { moviePreview ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.MovieClicked(
                            movieId = moviePreview.id,
                            movieTitle = moviePreview.title,
                            section = MovieAnalyticsValues.SECTION_SIMILAR,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openMovieDetails(moviePreview.id)
                },
                onShowFeedback = handleShowFeedback,
                modifier = Modifier.topSectionSpacing()
            )

            movieRecommendationsSection(
                movies = movie.recommendations,
                onMovieClick = { moviePreview ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.MovieClicked(
                            movieId = moviePreview.id,
                            movieTitle = moviePreview.title,
                            section = MovieAnalyticsValues.SECTION_RECOMMENDED,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openMovieDetails(moviePreview.id)
                },
                onShowFeedback = handleShowFeedback,
                modifier = Modifier.topSectionSpacing()
            )

            movieKeywordsSection(
                keywords = movie.keywords,
                onKeywordClick = { keyword ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.KeywordClicked(
                            keyword = keyword,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                        )
                    )
                    openMovieList(
                        MovieListingArgs.ByKeyword(
                            title = keyword.name,
                            keywordId = keyword.id
                        )
                    )
                },
                modifier = Modifier.topSectionSpacing()
            )

            item(key = "movie_bottom_spacer", contentType = "bottom_spacer") {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        MovieDetailsOverlays(
            movie = movie,
            notificationPermissionHandler = notificationPermissionHandler,
            showLogDialog = showLogDialog,
            diaryEntries = diaryEntries,
            onDismissLogDialog = { showLogDialog = false },
            onSaveDiaryEntry = { entry ->
                val wasExisting = diaryEntries.isNotEmpty()
                viewModel.saveDiaryEntry(entry)
                showLogDialog = false
                coroutineScope.launch {
                    val result = snackbarHostState.showImmediateSnackbar(
                        message = if (wasExisting) diaryUpdatedMessage else diaryLoggedMessage,
                        actionLabel = viewInDiaryLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        openLibraryPage(CinemaDiaryNavKey)
                    }
                }
            },
            selectedProviderPayload = selectedProviderPayload,
            watchProviderRegion = watchProviderRegion,
            affiliateRepository = viewModel.affiliateRepository,
            onDismissProviderAction = viewModel::dismissProviderAction,
            onBrowseWatchHub = { provider -> openWatchHub(provider) },
            activeGateConfig = activeGateConfig,
            isAdLoading = isAdLoading,
            isProPaymentEnabled = viewModel.billingRepository.isBillingEnabled.collectAsStateWithLifecycle().value,
            onWatchAd = {
                val activity = context.findActivity()
                if (activity != null) {
                    viewModel.onWatchAdForActiveGate(activity, movie)
                }
            },
            onUpgradeProClick = {
                viewModel.dismissQuotaGate()
                openProPaywall()
            },
            onDismissQuotaGate = { viewModel.dismissQuotaGate() },
            isReminderSheetVisible = isReminderSheetVisible,
            hasReminder = hasReminder,
            reminderLeadDays = reminderLeadDays,
            reminderNotificationHour = reminderNotificationHour,
            reminderNotificationMinute = reminderNotificationMinute,
            onScheduleReminder = { leadDays, hour, minute ->
                viewModel.scheduleReminder(movie, leadDays, hour, minute)
            },
            onRemoveReminder = {
                viewModel.removeReminder(movie)
            },
            onDismissReminderSheet = viewModel::dismissReminderSheet
        )
    }
}
