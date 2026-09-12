package com.ssverma.feature.movie.ui.details

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.component.notification.NotificationPermissionDialogs
import com.ssverma.shared.ui.component.notification.rememberNotificationPermissionHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.common.ui.community.MediaDiscussionsSection
import com.ssverma.shared.ads.gate.FeatureGateConfig
import com.ssverma.shared.ads.gate.FeaturePassPolicy
import com.ssverma.shared.ads.gate.GatePresentationStyle
import com.ssverma.shared.ads.gate.ShowTimeFeatureGate
import com.ssverma.shared.ads.quota.PassKey
import com.ssverma.core.ads.ui.rememberNativeAd
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.util.findActivity
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.HorizontalLazyListSection
import com.ssverma.core.ui.layout.SectionHeader
import androidx.navigation3.runtime.NavKey
import com.ssverma.feature.library.navigation.CinemaDiaryNavKey
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.analytics.MovieAnalyticsEvent
import com.ssverma.feature.movie.analytics.MovieAnalyticsScreenName
import com.ssverma.feature.movie.analytics.MovieAnalyticsValues
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.community.DiscussionNavArgs
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.ShareMediaUtils
import com.ssverma.shared.ui.component.media.menu.MediaOmniMenuConfig
import com.ssverma.shared.ui.component.BackdropActionButton
import com.ssverma.shared.ui.component.BackdropHeader
import com.ssverma.shared.ui.component.GenreItem
import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.diary.LogAndRateDialog
import com.ssverma.shared.ui.component.media.MediaItemDefaults
import com.ssverma.shared.ui.component.media.ShowFeedbackArgs
import com.ssverma.shared.ui.component.media.UniversalMediaCard
import com.ssverma.shared.ui.component.media.asUniversalMediaItem
import com.ssverma.shared.ui.component.media.menu.MediaOmniActionMenu
import com.ssverma.shared.ui.component.section.CreditSection
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.MediaReactionsSection
import com.ssverma.shared.ui.component.section.OverviewSection
import com.ssverma.shared.ui.component.section.ReviewsSection
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionVerticalSpacing
import com.ssverma.shared.ui.component.section.TagsSection
import com.ssverma.shared.ui.component.section.VideoShotsSection
import com.ssverma.shared.ui.component.section.WatchProvidersSection
import com.ssverma.shared.ui.component.section.WhereToWatchActionBottomSheet
import com.ssverma.shared.ui.emptyIfAbsent
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
fun MovieContent(
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
    val isQuotaGateVisible by viewModel.isQuotaGateVisible.collectAsStateWithLifecycle()
    val isAdLoading by viewModel.isAdLoading.collectAsStateWithLifecycle()
    val reminderSnackbarEvent by viewModel.reminderSnackbarEvent.collectAsStateWithLifecycle()
    var showLogDialog by remember { mutableStateOf(false) }
    val analytics = LocalAnalytics.current
    val watchProviderAd = rememberNativeAd(analyticsEventPrefix = "movie_details_watch_provider")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val notificationPermissionHandler = rememberNotificationPermissionHandler()

    LaunchedEffect(reminderSnackbarEvent) {
        reminderSnackbarEvent?.let { message ->
            snackbarHostState.showImmediateSnackbar(message)
            viewModel.clearReminderSnackbarEvent()
        }
    }

    Scaffold(
        snackbarHost = { ShowTimeSnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                BackdropHeader(
                    backdropImageUrl = movie.backdropImageUrl,
                    onCloseIconClick = onBackPressed,
                    showTrailerFab = movie.primaryTrailer != null,
                    onTrailerFabClick = {
                        analytics.logEvent(
                            MovieAnalyticsEvent.TrailerClicked(
                                movieId = movie.id,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                        viewModel.onPlayTrailerClicked(movie)
                    },
                    secondaryActions = {
                        if (!movie.isUpcoming) {
                            BackdropActionButton(
                                onClick = { showLogDialog = true },
                                icon = if (diaryEntries.isNotEmpty()) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = stringResource(id = SharedR.string.log_and_rate_cd)
                            )
                        }
                        MediaOmniActionMenu(
                            mediaId = movie.id,
                            mediaType = MediaType.Movie,
                            title = movie.title,
                            posterImageUrl = movie.posterImageUrl,
                            backdropImageUrl = movie.backdropImageUrl,
                            voteAvg = movie.voteAvg,
                            releaseDate = movie.releaseDate?.toString().orEmpty(),
                            config = MediaOmniMenuConfig(
                                isUpcoming = movie.isUpcoming,
                                showReminder = false
                            ),
                            existingDiaryEntry = diaryEntries.firstOrNull(),
                            onLogToDiary = { showLogDialog = true },
                            onOpenDiscussions = openDiscussionsList,
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
                            onShowFeedback = { args ->
                                coroutineScope.launch {
                                    val result = snackbarHostState.showImmediateSnackbar(
                                        message = args.message,
                                        actionLabel = args.actionLabel,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        openLibraryPage(
                                            args.destination ?: LibraryHomeNavKey.Default
                                        )
                                    }
                                }
                            },
                            actionContent = { onClick ->
                                BackdropActionButton(
                                    onClick = onClick,
                                    icon = Icons.Rounded.Add,
                                    contentDescription = stringResource(id = SharedR.string.more_options_cd)
                                )
                            }
                        )
                        if (movie.isUpcoming || hasReminder) {
                            BackdropActionButton(
                                onClick = {
                                    if (hasReminder) {
                                        viewModel.toggleReminder(movie)
                                    } else {
                                        notificationPermissionHandler.requestPermissionThen {
                                            viewModel.toggleReminder(movie)
                                        }
                                    }
                                },
                                icon = if (hasReminder) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                                containerColor = if (hasReminder) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                contentColor = if (hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                contentDescription = stringResource(id = if (hasReminder) SharedR.string.reminder_set else SharedR.string.remind_me)
                            )
                        }
                    }
                )
            }

            item {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp)
                )
            }

            movie.tagline?.let { tagline ->
                item {
                    Emphasize {
                        Text(
                            text = tagline,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 4.dp)
                        )
                    }
                }
            }

            item {
                Highlights(
                    highlights = remember(movie) { movie.highlightedItems() },
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            val watchProvider = movie.watchProviders[watchProviderRegion]
            if (watchProvider != null && watchProvider.hasProviders) {
                item(key = "watch_providers") {
                    WatchProvidersSection(
                        watchProvider = watchProvider,
                        modifier = Modifier.padding(top = SectionVerticalSpacing),
                        adContent = {
                            ShowTimeNativeAd(
                                ad = watchProviderAd,
                                loadInternally = false,
                                style = NativeAdStyle.CircularLogo,
                                modifier = Modifier.size(44.dp),
                                analyticsEventPrefix = "movie_details_watch_provider"
                            )
                        },
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
                        }
                    )
                }
            }

            item(key = "community_reactions") {
                MediaReactionsSection(
                    reactions = mediaReactions,
                    onTagClick = { tag ->
                        viewModel.onReactionTagClicked(tag = tag)
                    },
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            item {
                OverviewSection(
                    overview = movie.overview,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = SectionVerticalSpacing)
                )
            }

            item {
                HorizontalLazyList(
                    items = movie.generes,
                    contentPadding = PaddingValues(
                        top = SectionVerticalSpacing,
                        start = 16.dp,
                        end = 16.dp
                    )
                ) { genre ->
                    GenreItem(genre = genre) {
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
                    }
                }
            }

            item {
                CreditSection(
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
                    source = "movie_credit",
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            item {
                ImageShotsSection(
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
                    maxImageShots = 6,
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                )
            }

            item {
                VideoShotsSection(
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
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                )
            }

            item(key = "media_discussions") {
                MediaDiscussionsSection(
                    discussions = discussions,
                    onDiscussionsViewAllClick = openDiscussionsList,
                    onPostComment = viewModel::postComment,
                    onEditComment = viewModel::editComment,
                    onReportComment = viewModel::reportComment,
                    onToggleUpvote = viewModel::toggleCommentUpvote,
                    onDeleteComment = viewModel::deleteComment,
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            item {
                ReviewsSection(
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
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                    onReviewClick = {
                        analytics.logEvent(
                            MovieAnalyticsEvent.ReviewClicked(
                                review = it,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                    }
                )
            }

            item {
                RelevantMoviesSection(
                    movies = movie.similarMovies,
                    sectionTitleRes = R.string.similar_movies,
                    onMovieClick = { moviePreview ->
                        analytics.logEvent(
                            MovieAnalyticsEvent.MovieClicked(
                                movieId = moviePreview.id,
                                movieTitle = moviePreview.title,
                                section = MovieAnalyticsValues.SECTION_SIMILAR,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS,
                            )
                        )
                        openMovieDetails(moviePreview.id)
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
                    },
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                )
            }

            item {
                RelevantMoviesSection(
                    movies = movie.recommendations,
                    sectionTitleRes = R.string.recommendations,
                    onMovieClick = { moviePreview ->
                        analytics.logEvent(
                            MovieAnalyticsEvent.MovieClicked(
                                movieId = moviePreview.id,
                                movieTitle = moviePreview.title,
                                section = MovieAnalyticsValues.SECTION_RECOMMENDED,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS,
                            )
                        )
                        openMovieDetails(moviePreview.id)
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
                    },
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                )
            }

            item {
                TagsSection(
                    keywords = movie.keywords,
                    onClick = { keyword ->
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
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }

        NotificationPermissionDialogs(handler = notificationPermissionHandler)

        if (showLogDialog) {
            LogAndRateDialog(
                mediaId = movie.id,
                mediaType = MediaType.Movie,
                title = movie.title,
                posterImageUrl = movie.posterImageUrl,
                backdropImageUrl = movie.backdropImageUrl,
                releaseDate = movie.releaseDate?.toString().orEmpty(),
                tmdbRating = movie.voteAvg,
                existingEntry = diaryEntries.firstOrNull(),
                onDismiss = { showLogDialog = false },
                onSave = { entry ->
                    val wasExisting = diaryEntries.isNotEmpty()
                    viewModel.saveDiaryEntry(entry)
                    showLogDialog = false
                    coroutineScope.launch {
                        val result = snackbarHostState.showImmediateSnackbar(
                            message = context.getString(
                                if (wasExisting) {
                                    SharedR.string.media_menu_diary_updated_success
                                } else {
                                    SharedR.string.media_menu_diary_logged_success
                                },
                                movie.title
                            ),
                            actionLabel = context.getString(SharedR.string.media_menu_view_in_diary),
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            openLibraryPage(CinemaDiaryNavKey)
                        }
                    }
                }
            )
        }

        selectedProviderPayload?.let { payload ->
            val currentWatchProvider = movie.watchProviders[watchProviderRegion]
            WhereToWatchActionBottomSheet(
                provider = payload.provider,
                mediaTitle = movie.title,
                categoryName = payload.category,
                watchProviderLink = currentWatchProvider?.link,
                region = watchProviderRegion,
                affiliateRepository = viewModel.affiliateRepository,
                onDismissRequest = viewModel::dismissProviderAction,
                onBrowseHubClick = { provider ->
                    openWatchHub(provider)
                }
            )
        }

        if (isQuotaGateVisible) {
            ShowTimeFeatureGate(
                config = AiringReminderGateConfig,
                isAdLoading = isAdLoading,
                isProPaymentEnabled = viewModel.billingRepository.isBillingEnabled.collectAsStateWithLifecycle().value,
                onWatchAdClick = {
                    val activity = context.findActivity()
                    if (activity != null) {
                        viewModel.onWatchAdForReminderPass(activity, movie)
                    }
                },
                onUpgradeProClick = {
                    viewModel.dismissQuotaGate()
                    openProPaywall()
                },
                onDismissRequest = { viewModel.dismissQuotaGate() }
            )
        }
    }
}

private val AiringReminderPassKey = PassKey("airing_reminders")

private val AiringReminderGateConfig = FeatureGateConfig(
    titleRes = SharedR.string.reminder_quota_title,
    descriptionRes = SharedR.string.reminder_quota_desc,
    rewardActionLabelRes = SharedR.string.reminder_quota_reward_label,
    icon = Icons.Rounded.Lock,
    presentationStyle = GatePresentationStyle.BottomSheet,
    passPolicy = FeaturePassPolicy.ConsumableSlot(
        passKey = AiringReminderPassKey,
        slotsGranted = 1
    )
)

@Composable
fun RelevantMoviesSection(
    movies: List<Movie>,
    @StringRes sectionTitleRes: Int,
    onMovieClick: (movie: Movie) -> Unit,
    onShowFeedback: (ShowFeedbackArgs) -> Unit = {},
    modifier: Modifier = Modifier
) {
    HorizontalLazyListSection(
        items = movies,
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = sectionTitleRes),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        itemContent = {
            UniversalMediaCard(
                item = it.asUniversalMediaItem(),
                isGridView = true,
                onShowFeedback = onShowFeedback,
                onClick = { onMovieClick(it) },
                modifier = Modifier.width(MediaItemDefaults.PosterWidth)
            )
        },
        hideIf = movies.isEmpty(),
        modifier = modifier
    )
}

private fun Movie.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent()
        ),
        Highlight(
            labelRes = R.string.release_date,
            value = displayReleaseDate.orEmpty(),
        ),
        Highlight(
            labelRes = R.string.status,
            value = status
        ),
        Highlight(
            labelRes = R.string.language,
            value = originalLanguage
        ),
        Highlight(
            labelRes = R.string.runtime,
            value = if (runtime == 0) 0.emptyIfAbsent() else DateUtils.formatMinutes(runtime)
        ),
        Highlight(
            labelRes = R.string.revenue,
            value = if (revenue == 0L) 0.emptyIfAbsent() else "$$revenue"
        )
    )
}
