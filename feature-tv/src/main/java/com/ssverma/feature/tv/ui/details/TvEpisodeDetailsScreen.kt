package com.ssverma.feature.tv.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.DriveCompose
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.analytics.TvAnalyticsEvent
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.tv.TvEpisode
import com.ssverma.shared.ui.TmdbBackdropAspectRatio
import com.ssverma.shared.ui.bottomsheet.ImageShotBottomSheet
import com.ssverma.shared.ui.bottomsheet.SheetContentType
import com.ssverma.shared.ui.bottomsheet.rememberImageShotBottomSheetState
import com.ssverma.shared.ui.component.BackdropNavigationAction
import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.section.CreditSection
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.MediaDiscussionsSection
import com.ssverma.shared.ui.component.section.OverviewSection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvEpisodeDetailsScreen(
    onBackPress: () -> Unit,
    openPersonDetails: (Cast) -> Unit,
    viewModel: TvEpisodeDetailsViewModel,
    openDiscussionsList: (tvShowId: Int, seasonNumber: Int, episodeNumber: Int, episodeTitle: String?, posterImageUrl: String?, backdropImageUrl: String?) -> Unit = { _, _, _, _, _, _ -> }
) {
    val imageSheetState = rememberImageShotBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    val tvEpisodeUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isWatched by viewModel.isWatched.collectAsStateWithLifecycle()
    val discussions by viewModel.discussions.collectAsStateWithLifecycle()

    TrackScreenView(screenName = TvAnalyticsScreenName.TV_EPISODE)

    Surface(color = MaterialTheme.colorScheme.background) {
        DriveCompose(
            uiState = tvEpisodeUiState,
            onRetry = { viewModel.fetchTvEpisode() }
        ) { episode ->
            ImageShotBottomSheet(
                imageShots = episode.stills,
                sheetState = imageSheetState,
            ) {
                TvEpisodeContent(
                    episode = episode,
                    isWatched = isWatched,
                    discussions = discussions,
                    onToggleWatched = { viewModel.toggleWatched() },
                    onDiscussionsViewAllClick = {
                        openDiscussionsList(
                            viewModel.tvShowId,
                            episode.seasonNumber,
                            episode.episodeNumber,
                            episode.title,
                            episode.posterImageUrl,
                            null
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
                    modifier = Modifier
                        .padding(it)
                )
            }
        }
    }
}

@Composable
private fun TvEpisodeContent(
    episode: TvEpisode,
    isWatched: Boolean,
    discussions: List<Comment>,
    onToggleWatched: () -> Unit,
    onDiscussionsViewAllClick: () -> Unit,
    onPostComment: (content: String, isSpoiler: Boolean) -> Unit,
    onEditComment: (commentId: String, newContent: String, isSpoiler: Boolean) -> Unit,
    onReportComment: (commentId: String, reason: String) -> Unit,
    onToggleUpvote: (commentId: String) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    onBackPress: () -> Unit,
    openPersonDetails: (Cast) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics = LocalAnalytics.current

    LazyColumn(modifier = modifier) {
        item {
            BackdropHeader(
                backdropImageUrl = episode.posterImageUrl,
                onBackPress = onBackPress
            )
        }

        item {
            Text(
                text = episode.title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .padding(horizontal = 16.dp)
            ) {
                FilledTonalButton(
                    onClick = onToggleWatched,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isWatched) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isWatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.2f
                            ),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = if (isWatched) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isWatched) "Watched" else "Mark as Watched",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isWatched) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Highlights(
                highlights = remember { episode.highlightedItems() },
                modifier = Modifier
                    .padding(top = SectionSpacing)
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            OverviewSection(
                overview = episode.overview,
                modifier = Modifier
                    .padding(top = SectionSpacing)
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            ImageShotsSection(
                imageShots = episode.stills,
                maxImageShots = MaxImageShots,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                modifier = Modifier.padding(top = SectionSpacing)
            )
        }

        item {
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
                source = "tv_episode_credit",
                modifier = Modifier.padding(top = SectionSpacing)
            )
        }

        item {
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
                source = "tv_episode_credit",
                modifier = Modifier.padding(top = SectionSpacing)
            )
        }

        /*Episode Community Discussions*/
        item(key = "media_discussions") {
            MediaDiscussionsSection(
                discussions = discussions,
                onDiscussionsViewAllClick = onDiscussionsViewAllClick,
                onPostComment = onPostComment,
                onEditComment = onEditComment,
                onReportComment = onReportComment,
                onToggleUpvote = onToggleUpvote,
                onDeleteComment = onDeleteComment,
                modifier = Modifier.padding(top = SectionSpacing)
            )
        }

        item {
            Spacer(modifier = Modifier.height(SectionSpacing))
        }
    }
}

@Composable
private fun BackdropHeader(
    backdropImageUrl: String,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        NetworkImage(
            url = backdropImageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(TmdbBackdropAspectRatio)
        )

        /*Navigation*/
        BackdropNavigationAction(onIconClick = onBackPress)

        /*Rounded surface*/
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SurfaceCornerRoundSize)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium.copy(
                        topStart = CornerSize(SurfaceCornerRoundSize),
                        topEnd = CornerSize(SurfaceCornerRoundSize),
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    ),
                )
                .align(Alignment.BottomCenter)
        )
    }
}

private fun TvEpisode.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.season_number,
            value = seasonNumber.toString()
        ),
        Highlight(
            labelRes = R.string.episode_number,
            value = episodeNumber.toString()
        ),
        Highlight(
            labelRes = R.string.air_date,
            value = displayAirDate.orEmpty()
        ),
    )
}

private val SectionSpacing = 20.dp
private val SurfaceCornerRoundSize = 12.dp
private const val MaxImageShots = 3
