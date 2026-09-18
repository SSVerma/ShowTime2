package com.ssverma.feature.tv.ui.details

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
import com.ssverma.feature.tv.analytics.TvAnalyticsEvent
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.feature.tv.analytics.TvAnalyticsValues
import com.ssverma.feature.tv.navigation.args.TvSeasonArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.ui.details.component.TvShowDetailsOverlays
import com.ssverma.feature.tv.ui.details.component.tvCreditsSection
import com.ssverma.feature.tv.ui.details.component.tvDiscussionsSection
import com.ssverma.feature.tv.ui.details.component.tvGenresSection
import com.ssverma.feature.tv.ui.details.component.tvImageShotsSection
import com.ssverma.feature.tv.ui.details.component.tvKeywordsSection
import com.ssverma.feature.tv.ui.details.component.tvOverviewSection
import com.ssverma.feature.tv.ui.details.component.tvReactionsSection
import com.ssverma.feature.tv.ui.details.component.tvRecommendationsSection
import com.ssverma.feature.tv.ui.details.component.tvReviewsSection
import com.ssverma.feature.tv.ui.details.component.tvSeasonsSection
import com.ssverma.feature.tv.ui.details.component.tvShowAiringTimelineSection
import com.ssverma.feature.tv.ui.details.component.tvShowDetailsHeroSection
import com.ssverma.feature.tv.ui.details.component.tvSimilarShowsSection
import com.ssverma.feature.tv.ui.details.component.tvVideoShotsSection
import com.ssverma.feature.tv.ui.details.component.tvWatchProvidersSection
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.community.DiscussionNavArgs
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.utils.ShareMediaUtils
import com.ssverma.shared.ui.component.media.ShowFeedbackArgs
import com.ssverma.shared.ui.component.notification.rememberNotificationPermissionHandler
import com.ssverma.shared.ui.component.section.topSectionSpacing
import kotlinx.coroutines.launch
import com.ssverma.shared.ui.R as SharedR

@Composable
fun TvShowDetailsScreen(
    onBackPressed: () -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: (tvShowId: Int) -> Unit,
    openDiscussionsList: (DiscussionNavArgs) -> Unit = {},
    openPersonDetails: (Cast) -> Unit,
    openTvShowList: (listingRoute: TvShowListingRoute) -> Unit,
    openTvSeasonDetails: (seasonArgs: TvSeasonArgs) -> Unit,
    openWatchHub: (providerInfo: ProviderInfo) -> Unit,
    openLibraryPage: (NavKey) -> Unit = {},
    openProPaywall: () -> Unit = {},
    viewModel: TvShowDetailsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrackScreenView(screenName = TvAnalyticsScreenName.TV_DETAILS)

    Surface(color = MaterialTheme.colorScheme.background) {
        DriveCompose(
            uiState = uiState,
            onRetry = { viewModel.fetchTvShowDetails() }
        ) { data ->
            TvShowContent(
                tvShow = data.tvShow,
                viewModel = viewModel,
                uiState = data,
                onBackPressed = onBackPressed,
                openTvShowDetails = openTvShowDetails,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                openReviewsList = { openReviewsList(data.tvShow.id) },
                openDiscussionsList = {
                    openDiscussionsList(
                        DiscussionNavArgs(
                            mediaType = MediaType.Tv,
                            mediaId = data.tvShow.id,
                            title = data.tvShow.title,
                            posterImageUrl = data.tvShow.posterImageUrl,
                            backdropImageUrl = data.tvShow.backdropImageUrl
                        )
                    )
                },
                openYoutube = { videoId -> viewModel.openYoutubeApp(videoId = videoId) },
                openPersonDetails = openPersonDetails,
                openTvShowList = openTvShowList,
                openTvSeasonDetails = openTvSeasonDetails,
                openWatchHub = openWatchHub,
                openLibraryPage = openLibraryPage,
                openProPaywall = openProPaywall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TvShowContent(
    tvShow: TvShow,
    viewModel: TvShowDetailsViewModel,
    uiState: TvShowDetailsData,
    onBackPressed: () -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: () -> Unit,
    openDiscussionsList: () -> Unit,
    openYoutube: (videoId: String) -> Unit,
    openPersonDetails: (Cast) -> Unit,
    openTvShowList: (listingRoute: TvShowListingRoute) -> Unit,
    openTvSeasonDetails: (seasonArgs: TvSeasonArgs) -> Unit,
    openWatchHub: (providerInfo: ProviderInfo) -> Unit,
    openLibraryPage: (NavKey) -> Unit = {},
    openProPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val watchProviderRegion by viewModel.watchProviderRegion.collectAsStateWithLifecycle()
    val selectedProviderPayload by viewModel.selectedProviderForAction.collectAsStateWithLifecycle()
    val activeGateConfig by viewModel.activeGateConfig.collectAsStateWithLifecycle()
    val isAdLoading by viewModel.isAdLoading.collectAsStateWithLifecycle()
    val isBillingEnabled by viewModel.billingRepository.isBillingEnabled.collectAsStateWithLifecycle()
    val isReminderSheetVisible by viewModel.isReminderSheetVisible.collectAsStateWithLifecycle()
    val reminderLeadDays by viewModel.reminderLeadDays.collectAsStateWithLifecycle()
    val reminderNotificationHour by viewModel.reminderNotificationHour.collectAsStateWithLifecycle()
    val reminderNotificationMinute by viewModel.reminderNotificationMinute.collectAsStateWithLifecycle()
    val seasonWatchCounts by viewModel.seasonWatchCounts.collectAsStateWithLifecycle()
    val mediaReactions by viewModel.mediaReactions.collectAsStateWithLifecycle()
    val discussions by viewModel.discussions.collectAsStateWithLifecycle()
    val diaryEntries by viewModel.diaryEntries.collectAsStateWithLifecycle()
    val hasReminder by viewModel.hasReminder.collectAsStateWithLifecycle()
    val reminderSnackbarEvent by viewModel.reminderSnackbarEvent.collectAsStateWithLifecycle()
    var showLogDialog by remember { mutableStateOf(false) }
    val analytics = LocalAnalytics.current
    val watchProviderAd = rememberNativeAd(analyticsEventPrefix = "tv_details_watch_provider")
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
        tvShow.title
    )
    val diaryLoggedMessage = stringResource(
        SharedR.string.media_menu_diary_logged_success,
        tvShow.title
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
            tvShowDetailsHeroSection(
                tvShow = tvShow,
                hasReminder = hasReminder,
                existingDiaryEntry = diaryEntries.firstOrNull(),
                hasDiaryEntries = diaryEntries.isNotEmpty(),
                onBackPressed = onBackPressed,
                onPlayTrailer = {
                    analytics.logEvent(
                        TvAnalyticsEvent.TrailerClicked(
                            tvShowId = tvShow.id,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    viewModel.onPlayTrailerClicked(tvShow)
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
                        TvAnalyticsEvent.ShareClicked(
                            tvShowId = tvShow.id,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    val shareableText = ShareMediaUtils.buildShareableMediaText(
                        mediaTitle = tvShow.title,
                        mediaTagline = tvShow.tagline,
                        mediaOverview = tvShow.overview,
                        appPackageName = context.packageName,
                        mediaType = "tv",
                        mediaId = tvShow.id
                    )
                    context.dispatchShareTextIntent(text = shareableText)
                },
                onShowFeedback = handleShowFeedback
            )

            tvWatchProvidersSection(
                tvShow = tvShow,
                watchProviderRegion = watchProviderRegion,
                watchProviderAd = watchProviderAd,
                onWatchProviderClick = { providerInfo ->
                    analytics.logEvent(
                        TvAnalyticsEvent.WatchProviderClicked(
                            providerInfo = providerInfo,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openWatchHub(providerInfo)
                },
                onWatchProviderWithCategoryClick = { providerInfo, category ->
                    analytics.logEvent(
                        TvAnalyticsEvent.WatchProviderClicked(
                            providerInfo = providerInfo,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    viewModel.onProviderSelectedForAction(providerInfo, category)
                },
                onJustWatchClick = {
                    analytics.logEvent(
                        TvAnalyticsEvent.JustWatchClicked(
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                },
                modifier = Modifier.topSectionSpacing()
            )

            tvReactionsSection(
                mediaReactions = mediaReactions,
                onReactionTagClicked = viewModel::onReactionTagClicked,
                modifier = Modifier.topSectionSpacing()
            )

            tvOverviewSection(
                overview = tvShow.overview,
                modifier = Modifier
                    .topSectionSpacing()
                    .padding(horizontal = horizontalSpacing)
            )

            tvGenresSection(
                genres = tvShow.generes,
                onGenreClicked = { genre ->
                    analytics.logEvent(
                        TvAnalyticsEvent.GenreClicked(
                            genre = genre,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
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
                modifier = Modifier.topSectionSpacing()
            )

            tvShowAiringTimelineSection(
                nextEpisodeToAir = tvShow.nextEpisodeToAir,
                lastEpisodeToAir = tvShow.lastEpisodeToAir,
                hasReminder = hasReminder,
                onToggleReminder = {
                    if (hasReminder) {
                        viewModel.openReminderSheet()
                    } else {
                        notificationPermissionHandler.requestPermissionThen {
                            viewModel.openReminderSheet()
                        }
                    }
                },
                modifier = Modifier.topSectionSpacing()
            )

            tvSeasonsSection(
                seasons = tvShow.seasons,
                seasonWatchCounts = seasonWatchCounts,
                onSeasonClick = { season ->
                    analytics.logEvent(
                        TvAnalyticsEvent.SeasonClicked(
                            season = season,
                            tvShowId = tvShow.id,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openTvSeasonDetails(
                        TvSeasonArgs(
                            tvShowId = viewModel.tvShowId,
                            seasonNumber = season.seasonNumber,
                            tvShowTitle = tvShow.title,
                            tvShowPosterPath = tvShow.posterImageUrl,
                            tvShowBackdropPath = tvShow.backdropImageUrl
                        )
                    )
                },
                onToggleSeasonWatched = viewModel::toggleSeasonWatched,
                modifier = Modifier.topSectionSpacing()
            )

            tvCreditsSection(
                casts = tvShow.casts,
                onPersonClick = { cast ->
                    analytics.logEvent(
                        TvAnalyticsEvent.CastClicked(
                            cast = cast,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openPersonDetails(cast)
                },
                modifier = Modifier.topSectionSpacing()
            )

            tvImageShotsSection(
                imageShots = uiState.imageShots,
                openImageShotsList = {
                    analytics.logEvent(
                        TvAnalyticsEvent.SeeAllClicked(
                            section = TvAnalyticsValues.SECTION_SHOTS,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openImageShotsList()
                },
                openImageShot = { index ->
                    analytics.logEvent(
                        TvAnalyticsEvent.ImageShotClicked(
                            index = index,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openImageShot(index)
                },
                modifier = Modifier.topSectionSpacing()
            )

            tvVideoShotsSection(
                videos = tvShow.videos,
                onVideoClick = { video ->
                    analytics.logEvent(
                        TvAnalyticsEvent.VideoClicked(
                            video = video,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openYoutube(video.key)
                },
                modifier = Modifier.topSectionSpacing()
            )

            tvDiscussionsSection(
                discussions = discussions,
                onDiscussionsViewAllClick = openDiscussionsList,
                onPostComment = viewModel::postComment,
                onEditComment = viewModel::editComment,
                onReportComment = viewModel::reportComment,
                onToggleUpvote = viewModel::toggleCommentUpvote,
                onDeleteComment = viewModel::deleteComment,
                modifier = Modifier.topSectionSpacing()
            )

            tvReviewsSection(
                reviews = tvShow.reviews,
                onReviewsViewAllClick = {
                    analytics.logEvent(
                        TvAnalyticsEvent.SeeAllClicked(
                            section = TvAnalyticsValues.SECTION_REVIEWS,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openReviewsList()
                },
                onReviewClick = {
                    analytics.logEvent(
                        TvAnalyticsEvent.ReviewClicked(
                            review = it,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                },
                modifier = Modifier.topSectionSpacing()
            )

            tvSimilarShowsSection(
                tvShows = tvShow.similarTvShows,
                onTvShowClick = { tvShowPreview ->
                    analytics.logEvent(
                        TvAnalyticsEvent.TvShowClicked(
                            tvShowId = tvShowPreview.id,
                            tvShowTitle = tvShowPreview.title,
                            section = TvAnalyticsValues.SECTION_SIMILAR,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openTvShowDetails(tvShowPreview.id)
                },
                onShowFeedback = handleShowFeedback,
                modifier = Modifier.topSectionSpacing()
            )

            tvRecommendationsSection(
                tvShows = tvShow.recommendations,
                onTvShowClick = { tvShowPreview ->
                    analytics.logEvent(
                        TvAnalyticsEvent.TvShowClicked(
                            tvShowId = tvShowPreview.id,
                            tvShowTitle = tvShowPreview.title,
                            section = TvAnalyticsValues.SECTION_RECOMMENDED,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openTvShowDetails(tvShowPreview.id)
                },
                onShowFeedback = handleShowFeedback,
                modifier = Modifier.topSectionSpacing()
            )

            tvKeywordsSection(
                keywords = tvShow.keywords,
                onKeywordClick = { keyword ->
                    analytics.logEvent(
                        TvAnalyticsEvent.KeywordClicked(
                            keyword = keyword,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openTvShowList(
                        TvShowListingRoute(
                            args = TvShowListingArgs.ByKeyword(
                                keywordId = keyword.id,
                                title = keyword.name
                            )
                        )
                    )
                },
                modifier = Modifier.topSectionSpacing()
            )

            item(key = "tv_bottom_spacer", contentType = "bottom_spacer") {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        TvShowDetailsOverlays(
            tvShow = tvShow,
            notificationPermissionHandler = notificationPermissionHandler,
            showLogDialog = showLogDialog,
            diaryEntries = diaryEntries,
            onDismissLogDialog = { showLogDialog = false },
            onSaveDiaryEntry = { entry, wasExisting ->
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
            onBrowseWatchHub = openWatchHub,
            activeGateConfig = activeGateConfig,
            isAdLoading = isAdLoading,
            isProPaymentEnabled = isBillingEnabled,
            onWatchAd = {
                val activity = context.findActivity()
                if (activity != null) {
                    viewModel.onWatchAdForActiveGate(activity, tvShow)
                }
            },
            onUpgradeProClick = {
                viewModel.dismissQuotaGate()
                openProPaywall()
            },
            onDismissQuotaGate = viewModel::dismissQuotaGate,
            isReminderSheetVisible = isReminderSheetVisible,
            hasReminder = hasReminder,
            reminderLeadDays = reminderLeadDays,
            reminderNotificationHour = reminderNotificationHour,
            reminderNotificationMinute = reminderNotificationMinute,
            onScheduleReminder = { leadDays, hour, minute ->
                viewModel.scheduleReminder(tvShow, leadDays, hour, minute)
            },
            onRemoveReminder = {
                viewModel.removeReminder(tvShow)
            },
            onDismissReminderSheet = viewModel::dismissReminderSheet
        )
    }
}
