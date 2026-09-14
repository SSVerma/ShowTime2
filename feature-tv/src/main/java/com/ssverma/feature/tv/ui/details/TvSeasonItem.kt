package com.ssverma.feature.tv.ui.details

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val containerColor = when {
        isFullyWatched -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        isInProgress -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val actionButtonColor = when {
        isFullyWatched -> MaterialTheme.colorScheme.primary
        isInProgress -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val actionIconTint = when {
        isFullyWatched -> MaterialTheme.colorScheme.onPrimary
        isInProgress -> MaterialTheme.colorScheme.onTertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
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

                    if (isFullyWatched) {
                        TextBadge(
                            text = stringResource(id = R.string.season_all_watched),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else if (isInProgress) {
                        TextBadge(
                            text = stringResource(
                                id = R.string.progress_eps_format,
                                watchedEpisodeCount,
                                totalEpisodes
                            ),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        TextBadge(
                            text = stringResource(id = R.string.episodes_n, totalEpisodes),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                Surface(
                    shape = CircleShape,
                    color = actionButtonColor,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isInProgress) {
                            CircularProgressIndicator(
                                progress = {
                                    (watchedEpisodeCount.toFloat() / totalEpisodes.coerceAtLeast(1)).coerceIn(
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
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(
                                    id = if (isFullyWatched) R.string.season_all_watched else R.string.mark_season
                                ),
                                tint = actionIconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
