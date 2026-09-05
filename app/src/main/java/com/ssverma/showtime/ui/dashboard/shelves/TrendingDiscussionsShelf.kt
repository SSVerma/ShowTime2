package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.DiscussionNavArgs
import com.ssverma.shared.domain.model.community.TrendingDiscussion
import com.ssverma.shared.ui.R

fun LazyListScope.trendingDiscussionsShelf(
    discussions: List<TrendingDiscussion>,
    onDiscussionClick: (DiscussionNavArgs) -> Unit
) {
    if (discussions.isNotEmpty()) {
        item(key = "trending_discussions_shelf") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                // Shelf Header
                SectionHeader(
                    title = stringResource(id = R.string.trending_buzz_title),
                    leadingIcon = Icons.Rounded.Forum,
                    leadingIconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.7f
                    ),
                    leadingIconTint = MaterialTheme.colorScheme.primary,
                    titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(
                        items = discussions,
                        key = { index, item -> "${item.mediaType}_${item.mediaId}_${item.seasonNumber}_${item.episodeNumber}_$index" }
                    ) { _, item ->
                        TrendingDiscussionCard(
                            discussion = item,
                            onClick = {
                                onDiscussionClick(
                                    DiscussionNavArgs(
                                        mediaType = item.mediaType,
                                        mediaId = item.mediaId,
                                        title = item.title,
                                        posterImageUrl = item.posterImageUrl,
                                        backdropImageUrl = item.backdropImageUrl,
                                        seasonNumber = item.seasonNumber,
                                        episodeNumber = item.episodeNumber
                                    )
                                )
                            },
                            modifier = if (discussions.size == 1) {
                                Modifier.fillParentMaxWidth()
                            } else {
                                Modifier.width(290.dp)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingDiscussionCard(
    discussion: TrendingDiscussion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
        modifier = modifier.height(130.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Poster / Media Image Thumbnail
            val imageUrl = discussion.posterImageUrl ?: discussion.backdropImageUrl
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(88.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    NetworkImage(
                        url = imageUrl,
                        contentDescription = discussion.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (discussion.mediaType == MediaType.Tv) Icons.Rounded.LiveTv else Icons.Rounded.Movie,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Info & Quote Column
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Top Row: Media Type & Discussion Count Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(size = 6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        val seasonNumber = discussion.seasonNumber
                        val episodeNumber = discussion.episodeNumber
                        val badgeText = when {
                            seasonNumber != null && episodeNumber != null -> {
                                stringResource(
                                    id = R.string.discussion_badge_episode,
                                    seasonNumber,
                                    episodeNumber
                                )
                            }

                            discussion.mediaType == MediaType.Tv -> {
                                stringResource(id = R.string.discussion_badge_tv)
                            }

                            else -> {
                                stringResource(id = R.string.discussion_badge_movie)
                            }
                        }
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(size = 6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = discussion.discussionCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Media Title
                Text(
                    text = discussion.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Comment Snippet Bubble
                Surface(
                    shape = RoundedCornerShape(size = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = discussion.latestCommentSnippet?.let {
                            stringResource(
                                id = R.string.discussion_comment_quote,
                                it
                            )
                        } ?: stringResource(id = R.string.join_the_discussion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
