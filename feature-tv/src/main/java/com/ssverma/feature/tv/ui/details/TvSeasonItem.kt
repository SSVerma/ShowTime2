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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
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
        isFullyWatched -> Color(0xFF4CAF50).copy(alpha = 0.08f)
        isInProgress -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val actionButtonColor = when {
        isFullyWatched -> Color(0xFF4CAF50)
        isInProgress -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val actionIconTint = when {
        isFullyWatched -> Color.White
        isInProgress -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(88.dp)
                    .aspectRatio(TmdbPosterAspectRatio)
                    .clip(MaterialTheme.shapes.medium)
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = tvSeason.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                FlowRow(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tvSeason.displayAirDate?.let {
                        DateBadge(dateText = it)
                    }

                    if (isFullyWatched) {
                        TextBadge(
                            text = "✓ $totalEpisodes eps",
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            contentColor = Color(0xFF4CAF50)
                        )
                    } else if (isInProgress) {
                        TextBadge(
                            text = "$watchedEpisodeCount / $totalEpisodes eps",
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        TextBadge(
                            text = stringResource(id = R.string.episodes_n, totalEpisodes),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Text(
                    text = tvSeason.overview,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    fontStyle = FontStyle.Normal,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Intuitive Mark Season Watched / In-Progress Action Button
            IconButton(
                onClick = onToggleWatched,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(40.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = actionButtonColor,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isInProgress) {
                            CircularProgressIndicator(
                                progress = { (watchedEpisodeCount.toFloat() / totalEpisodes.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.25f),
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = "$watchedEpisodeCount",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = if (isFullyWatched) "Season watched" else "Mark season watched",
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
