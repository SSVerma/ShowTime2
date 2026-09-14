package com.ssverma.feature.tv.ui.details.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.tv.TvEpisode
import com.ssverma.shared.ui.TmdbBackdropAspectRatio
import com.ssverma.shared.ui.component.media.DateBadge
import com.ssverma.shared.ui.component.media.ScoreBadge

@Composable
fun TvEpisodeTimelineItem(
    tvEpisode: TvEpisode,
    isWatched: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    isUpNext: Boolean,
    onClick: () -> Unit,
    onToggleWatched: () -> Unit,
    modifier: Modifier = Modifier,
    isPrevWatched: Boolean = false,
    isNextWatched: Boolean = false
) {
    val upperProgress by animateFloatAsState(
        targetValue = if (isPrevWatched && isWatched) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "upper_line_progress"
    )

    val lowerProgress by animateFloatAsState(
        targetValue = if (isWatched && isNextWatched) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "lower_line_progress"
    )

    val cardContainerColor by animateColorAsState(
        targetValue = if (isWatched) {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)
        } else if (isUpNext) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = tween(durationMillis = 300),
        label = "card_container_color"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        // Vertical Timeline Line & Node with smooth animation
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight()
        ) {
            // Upper connector line
            if (!isFirst) {
                AnimatedTimelineConnector(
                    progress = upperProgress,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Timeline status node
            TimelineNode(
                isWatched = isWatched,
                isUpNext = isUpNext
            )

            // Lower connector line
            if (!isLast) {
                AnimatedTimelineConnector(
                    progress = lowerProgress,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

        // Episode Content Card
        Card(
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = cardContainerColor),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = MaterialTheme.spacing.extraSmall)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.small)
            ) {
                // Fixed-size 16:9 thumbnail with fallback placeholder
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .width(96.dp)
                        .aspectRatio(TmdbBackdropAspectRatio)
                        .clip(MaterialTheme.shapes.small)
                ) {
                    if (tvEpisode.posterImageUrl.isNotBlank()) {
                        NetworkImage(
                            url = tvEpisode.posterImageUrl,
                            contentDescription = tvEpisode.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tv,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Episode info column
                Column(
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.small)
                        .weight(1f)
                ) {
                    Text(
                        text = "${tvEpisode.episodeNumber}. ${tvEpisode.title}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isUpNext) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    FlowRow(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                    ) {
                        tvEpisode.displayAirDate?.let { dateText ->
                            DateBadge(dateText = dateText)
                        }
                        if (tvEpisode.voteAvg > 0f) {
                            ScoreBadge(score = (tvEpisode.voteAvg * 10))
                        }
                    }

                    if (tvEpisode.overview.isNotBlank()) {
                        Text(
                            text = tvEpisode.overview,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Mark Watched Button
                val buttonColor by animateColorAsState(
                    targetValue = if (isWatched) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "watch_button_color"
                )

                IconButton(
                    onClick = onToggleWatched,
                    modifier = Modifier.size(36.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = buttonColor,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(
                                    id = if (isWatched) R.string.episode_watched else R.string.mark_watched
                                ),
                                tint = if (isWatched) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedTimelineConnector(
    progress: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.width(2.dp)) {
        // Draw background track line
        drawLine(
            color = trackColor,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = size.width,
            cap = StrokeCap.Square
        )

        // Draw animated active progress line
        if (progress > 0f) {
            drawLine(
                color = color,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height * progress.coerceIn(0f, 1f)),
                strokeWidth = size.width,
                cap = StrokeCap.Square
            )
        }
    }
}

@Composable
private fun TimelineNode(
    isWatched: Boolean,
    isUpNext: Boolean,
    modifier: Modifier = Modifier
) {
    val nodeColor by animateColorAsState(
        targetValue = when {
            isWatched -> MaterialTheme.colorScheme.primary
            isUpNext -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 350),
        label = "timeline_node_color"
    )

    val nodeSize by animateDpAsState(
        targetValue = if (isUpNext) 16.dp else if (isWatched) 14.dp else 12.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "timeline_node_size"
    )

    Surface(
        shape = CircleShape,
        color = nodeColor,
        tonalElevation = if (isUpNext) 4.dp else 0.dp,
        modifier = modifier.size(nodeSize)
    ) {
        AnimatedVisibility(
            visible = isWatched,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(9.dp)
                )
            }
        }
    }
}
