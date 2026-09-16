package com.ssverma.feature.tv.ui.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.media.DateBadge
import com.ssverma.shared.ui.component.media.TextBadge

private enum class SeasonWatchState {
    AllWatched,
    InProgress,
    NotWatched
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TvSeasonItem(
    tvSeason: TvSeason,
    watchedEpisodeCount: Int = 0,
    onClick: () -> Unit,
    onToggleWatched: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val totalEpisodes = tvSeason.episodeCount
    val isFullyWatched = totalEpisodes > 0 && watchedEpisodeCount >= totalEpisodes
    val isInProgress = watchedEpisodeCount > 0 && watchedEpisodeCount < totalEpisodes

    val watchState = when {
        isFullyWatched -> SeasonWatchState.AllWatched
        isInProgress -> SeasonWatchState.InProgress
        else -> SeasonWatchState.NotWatched
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = when {
            isFullyWatched -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            isInProgress -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "season_container_color"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            isFullyWatched -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            isInProgress -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "season_border_color"
    )

    val animatedActionButtonColor by animateColorAsState(
        targetValue = when {
            isFullyWatched -> MaterialTheme.colorScheme.primary
            isInProgress -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "season_action_button_color"
    )

    val animatedActionIconTint by animateColorAsState(
        targetValue = when {
            isFullyWatched -> MaterialTheme.colorScheme.onPrimary
            isInProgress -> MaterialTheme.colorScheme.onTertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "season_action_icon_tint"
    )

    val checkButtonScale by animateFloatAsState(
        targetValue = if (isFullyWatched) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "season_check_scale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        border = BorderStroke(width = 1.dp, color = animatedBorderColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImage(
                url = tvSeason.posterImageUrl,
                contentDescription = tvSeason.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(88.dp)
                    .aspectRatio(TmdbPosterAspectRatio)
                    .clip(MaterialTheme.shapes.medium)
            )

            Column(
                modifier = Modifier
                    .padding(MaterialTheme.spacing.small)
                    .weight(1f)
            ) {
                Text(
                    text = tvSeason.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                FlowRow(
                    modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    tvSeason.displayAirDate?.let { dateText ->
                        DateBadge(dateText = dateText)
                    }

                    AnimatedContent(
                        targetState = watchState,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220, delayMillis = 40)) + scaleIn(
                                initialScale = 0.8f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )).togetherWith(
                                fadeOut(animationSpec = tween(160)) + scaleOut(
                                    targetScale = 0.8f,
                                    animationSpec = tween(160)
                                )
                            )
                        },
                        label = "season_status_badge"
                    ) { state ->
                        when (state) {
                            SeasonWatchState.AllWatched -> {
                                TextBadge(
                                    text = stringResource(id = R.string.season_all_watched),
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            SeasonWatchState.InProgress -> {
                                TextBadge(
                                    text = stringResource(
                                        id = R.string.progress_eps_format,
                                        watchedEpisodeCount,
                                        totalEpisodes
                                    ),
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            SeasonWatchState.NotWatched -> {
                                TextBadge(
                                    text = stringResource(id = R.string.episodes_n, totalEpisodes),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (tvSeason.overview.isNotBlank()) {
                    Text(
                        text = tvSeason.overview,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        fontStyle = FontStyle.Normal,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onToggleWatched,
                modifier = Modifier
                    .padding(end = MaterialTheme.spacing.extraSmall)
                    .size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            scaleX = checkButtonScale
                            scaleY = checkButtonScale
                        }
                        .clip(CircleShape)
                        .background(animatedActionButtonColor)
                ) {
                    AnimatedContent(
                        targetState = isFullyWatched to isInProgress,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(200)) + scaleIn(
                                initialScale = 0.6f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )).togetherWith(
                                fadeOut(animationSpec = tween(150)) + scaleOut(
                                    targetScale = 0.6f,
                                    animationSpec = tween(150)
                                )
                            )
                        },
                        label = "season_check_action_content"
                    ) { (fullyWatched, inProgress) ->
                        if (inProgress) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = {
                                        (watchedEpisodeCount.toFloat() / totalEpisodes.coerceAtLeast(
                                            1
                                        )).coerceIn(
                                            0f,
                                            1f
                                        )
                                    },
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    trackColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.25f),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "$watchedEpisodeCount",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(
                                    id = if (fullyWatched) R.string.season_all_watched else R.string.mark_season
                                ),
                                tint = animatedActionIconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
