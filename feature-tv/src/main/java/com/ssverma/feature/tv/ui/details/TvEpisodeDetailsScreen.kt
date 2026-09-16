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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.common.ui.community.MediaDiscussionsSection
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.analytics.TvAnalyticsEvent
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.feature.tv.navigation.args.TvEpisodeArgs
import com.ssverma.feature.tv.ui.details.component.TvEpisodeHeroHeader
import com.ssverma.feature.tv.ui.details.component.TvEpisodeTimelineNavSection
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DiscussionNavArgs
import com.ssverma.shared.domain.model.community.EditCommentArgs
import com.ssverma.shared.domain.model.community.PostCommentArgs
import com.ssverma.shared.domain.model.community.ReportCommentArgs
import com.ssverma.shared.domain.model.tv.TvEpisode
import com.ssverma.shared.ui.bottomsheet.ImageShotBottomSheet
import com.ssverma.shared.ui.bottomsheet.SheetContentType
import com.ssverma.shared.ui.bottomsheet.rememberImageShotBottomSheetState
import com.ssverma.shared.ui.component.section.CreditSection
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.OverviewSection
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionVerticalSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvEpisodeDetailsScreen(
    onBackPress: () -> Unit,
    openPersonDetails: (Cast) -> Unit,
    viewModel: TvEpisodeDetailsViewModel,
    onPreviousEpisodeClick: (TvEpisodeArgs) -> Unit = {},
    onNextEpisodeClick: (TvEpisodeArgs) -> Unit = {},
    openDiscussionsList: (DiscussionNavArgs) -> Unit = {}
) {
    val imageSheetState = rememberImageShotBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val analytics = LocalAnalytics.current

    val tvEpisodeUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isWatched by viewModel.isWatched.collectAsStateWithLifecycle()
    val discussions by viewModel.discussions.collectAsStateWithLifecycle()

    TrackScreenView(screenName = TvAnalyticsScreenName.TV_EPISODE)

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        DriveCompose(
            uiState = tvEpisodeUiState,
            onRetry = { viewModel.fetchTvEpisode() }
        ) { episode ->
            ImageShotBottomSheet(
                imageShots = episode.stills,
                sheetState = imageSheetState
            ) {
                TvEpisodeContent(
                    episode = episode,
                    tvShowTitle = viewModel.tvShowTitle,
                    tvShowBackdropPath = viewModel.tvShowBackdropPath,
                    isWatched = isWatched,
                    discussions = discussions,
                    onToggleWatched = { viewModel.toggleWatched() },
                    onShareClick = {
                        analytics.logEvent(
                            TvAnalyticsEvent.ShareClicked(
                                tvShowId = viewModel.tvShowId,
                                sourceScreen = TvAnalyticsScreenName.TV_EPISODE
                            )
                        )
                        val shareText = context.getString(
                            R.string.share_episode_text,
                            viewModel.tvShowTitle ?: episode.title,
                            episode.title,
                            episode.seasonNumber,
                            episode.episodeNumber
                        )
                        context.dispatchShareTextIntent(text = shareText)
                    },
                    onPreviousEpisodeClick = {
                        onPreviousEpisodeClick(
                            TvEpisodeArgs(
                                tvShowId = viewModel.tvShowId,
                                seasonNumber = episode.seasonNumber,
                                episodeNumber = episode.episodeNumber - 1,
                                tvShowTitle = viewModel.tvShowTitle,
                                tvShowPosterPath = viewModel.tvShowPosterPath,
                                tvShowBackdropPath = viewModel.tvShowBackdropPath
                            )
                        )
                    },
                    onNextEpisodeClick = {
                        onNextEpisodeClick(
                            TvEpisodeArgs(
                                tvShowId = viewModel.tvShowId,
                                seasonNumber = episode.seasonNumber,
                                episodeNumber = episode.episodeNumber + 1,
                                tvShowTitle = viewModel.tvShowTitle,
                                tvShowPosterPath = viewModel.tvShowPosterPath,
                                tvShowBackdropPath = viewModel.tvShowBackdropPath
                            )
                        )
                    },
                    onDiscussionsViewAllClick = {
                        openDiscussionsList(
                            DiscussionNavArgs(
                                mediaType = MediaType.Tv,
                                mediaId = viewModel.tvShowId,
                                seasonNumber = episode.seasonNumber,
                                episodeNumber = episode.episodeNumber,
                                title = episode.title,
                                posterImageUrl = episode.posterImageUrl,
                                backdropImageUrl = null
                            )
                        )
                    },
                    onPostComment = viewModel::postComment,
                    onEditComment = viewModel::editComment,
                    onReportComment = viewModel::reportComment,
                    onToggleUpvote = viewModel::toggleCommentUpvote,
                    onDeleteComment = viewModel::deleteComment,
                    onBackPress = onBackPress,
                    openPersonDetails = openPersonDetails,
                    openImageShotsList = {
                        coroutineScope.launch {
                            imageSheetState.show(SheetContentType.ImageList)
                        }
                    },
                    openImageShot = { pageIndex ->
                        coroutineScope.launch {
                            imageSheetState.show(SheetContentType.ImagePager(pageIndex))
                        }
                    },
                    modifier = Modifier.padding(it)
                )
            }
        }
    }
}

@Composable
private fun TvEpisodeContent(
    episode: TvEpisode,
    tvShowTitle: String?,
    tvShowBackdropPath: String?,
    isWatched: Boolean,
    discussions: List<Comment>,
    onToggleWatched: () -> Unit,
    onShareClick: () -> Unit,
    onPreviousEpisodeClick: () -> Unit,
    onNextEpisodeClick: () -> Unit,
    onDiscussionsViewAllClick: () -> Unit,
    onPostComment: (PostCommentArgs) -> Unit,
    onEditComment: (EditCommentArgs) -> Unit,
    onReportComment: (ReportCommentArgs) -> Unit,
    onToggleUpvote: (commentId: String) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    onBackPress: () -> Unit,
    openPersonDetails: (Cast) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics = LocalAnalytics.current

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Full Width Hero Header (Episode still backdrop, show breadcrumbs, title, watched action)
            item(key = "episode_hero_header") {
                TvEpisodeHeroHeader(
                    episode = episode,
                    tvShowTitle = tvShowTitle,
                    tvShowBackdropPath = tvShowBackdropPath,
                    isWatched = isWatched,
                    onBackPress = onBackPress,
                    onShareClick = onShareClick,
                    onToggleWatched = onToggleWatched
                )
            }

            // Overview Section
            if (episode.overview.isNotBlank()) {
                item(key = "episode_overview") {
                    OverviewSection(
                        overview = episode.overview,
                        modifier = Modifier
                            .padding(top = SectionVerticalSpacing)
                            .padding(horizontal = MaterialTheme.spacing.medium)
                    )
                }
            }

            // Timeline Navigation (Previous / Next Episode)
            item(key = "episode_timeline_nav") {
                TvEpisodeTimelineNavSection(
                    currentEpisodeNumber = episode.episodeNumber,
                    onPreviousEpisodeClick = onPreviousEpisodeClick,
                    onNextEpisodeClick = onNextEpisodeClick,
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            // Image Stills / Shots (Max 3)
            if (episode.stills.isNotEmpty()) {
                item(key = "episode_shots") {
                    ImageShotsSection(
                        imageShots = episode.stills,
                        maxImageShots = 3,
                        openImageShotsList = openImageShotsList,
                        openImageShot = openImageShot,
                        modifier = Modifier.padding(top = SectionVerticalSpacing)
                    )
                }
            }

            // Regular Casts
            if (episode.casts.isNotEmpty()) {
                item(key = "episode_casts") {
                    CreditSection(
                        casts = episode.casts,
                        onPersonClick = { cast ->
                            analytics.logEvent(
                                TvAnalyticsEvent.CastClicked(
                                    cast = cast,
                                    sourceScreen = TvAnalyticsScreenName.TV_EPISODE
                                )
                            )
                            openPersonDetails(cast)
                        },
                        source = "tv_episode_${episode.id}_credit",
                        enableSharedTransition = false,
                        modifier = Modifier.padding(top = SectionVerticalSpacing)
                    )
                }
            }

            // Guest Stars
            if (episode.guestStars.isNotEmpty()) {
                item(key = "episode_guest_stars") {
                    CreditSection(
                        casts = episode.guestStars,
                        titleRes = R.string.guest_appearance,
                        onPersonClick = { cast ->
                            analytics.logEvent(
                                TvAnalyticsEvent.CastClicked(
                                    cast = cast,
                                    sourceScreen = TvAnalyticsScreenName.TV_EPISODE
                                )
                            )
                            openPersonDetails(cast)
                        },
                        source = "tv_episode_${episode.id}_guest_stars",
                        enableSharedTransition = false,
                        modifier = Modifier.padding(top = SectionVerticalSpacing)
                    )
                }
            }

            // Episode Community Discussions
            item(key = "episode_discussions") {
                MediaDiscussionsSection(
                    discussions = discussions,
                    onDiscussionsViewAllClick = onDiscussionsViewAllClick,
                    onPostComment = onPostComment,
                    onEditComment = onEditComment,
                    onReportComment = onReportComment,
                    onToggleUpvote = onToggleUpvote,
                    onDeleteComment = onDeleteComment,
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(SectionVerticalSpacing))
            }
        }
    }
}
