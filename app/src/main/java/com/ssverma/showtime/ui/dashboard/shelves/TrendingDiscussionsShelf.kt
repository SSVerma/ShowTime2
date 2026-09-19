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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.DiscussionNavArgs
import com.ssverma.shared.domain.model.community.TrendingDiscussion
import com.ssverma.shared.ui.R

fun LazyListScope.trendingDiscussionsShelf(
    discussions: List<TrendingDiscussion>,
    onDiscussionClick: (DiscussionNavArgs) -> Unit,
    modifier: Modifier = Modifier
) {
    if (discussions.isNotEmpty()) {
        item(key = "trending_discussions_shelf") {
            Column(
                modifier = modifier.fillMaxWidth()
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
                                Modifier.width(300.dp)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            // Top Header: Media Thumbnail + Title/Badge + Discussion Count Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Media Thumbnail Poster
                val imageUrl = discussion.posterImageUrl ?: discussion.backdropImageUrl
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.size(width = 38.dp, height = 50.dp)
                ) {
                    if (!imageUrl.isNullOrBlank()) {
                        NetworkImage(
                            url = imageUrl,
                            contentDescription = discussion.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (discussion.mediaType == MediaType.Tv) Icons.Rounded.LiveTv else Icons.Rounded.Movie,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))

                // Title and Media Type Badge
                Column(
                    modifier = Modifier.weight(1f)
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
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = discussion.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                // Discussion Count Pill
                Surface(
                    shape = RoundedCornerShape(size = 8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = discussion.discussionCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

            // Hero Conversation Speech Bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 14.dp,
                    bottomEnd = 14.dp,
                    bottomStart = 14.dp
                ),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.smallMedium,
                        vertical = MaterialTheme.spacing.small
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Forum,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(15.dp)
                            .padding(top = 1.dp)
                    )

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                    val snippet = discussion.latestCommentSnippet
                    val hasSnippet = !snippet.isNullOrBlank()

                    Text(
                        text = if (hasSnippet) {
                            stringResource(id = R.string.discussion_comment_quote, snippet)
                        } else {
                            stringResource(id = R.string.join_the_discussion)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (hasSnippet) FontWeight.Normal else FontWeight.Medium,
                        color = if (hasSnippet) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            // Footer Action Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.discussion_card_join_prompt),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
