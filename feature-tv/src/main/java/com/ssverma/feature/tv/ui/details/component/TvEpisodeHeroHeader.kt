package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.tv.TvEpisode
import com.ssverma.shared.ui.R as SharedR
import com.ssverma.shared.ui.component.BackdropActionButton
import com.ssverma.shared.ui.component.BackdropHeader
import com.ssverma.shared.ui.component.media.DateBadge
import com.ssverma.shared.ui.component.media.ScoreBadge

@Composable
fun TvEpisodeHeroHeader(
    episode: TvEpisode,
    tvShowTitle: String?,
    tvShowBackdropPath: String?,
    isWatched: Boolean,
    onBackPress: () -> Unit,
    onShareClick: () -> Unit,
    onToggleWatched: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroBackdrop = if (episode.posterImageUrl.isNotBlank()) {
        episode.posterImageUrl
    } else {
        tvShowBackdropPath.orEmpty()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Full width Backdrop Header
        BackdropHeader(
            backdropImageUrl = heroBackdrop,
            onCloseIconClick = onBackPress,
            showTrailerFab = false,
            onTrailerFabClick = {},
            secondaryActions = {
                BackdropActionButton(
                    onClick = onShareClick,
                    icon = Icons.Rounded.Share,
                    contentDescription = stringResource(id = SharedR.string.share)
                )
            }
        )

        // Show context breadcrumbs
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(top = MaterialTheme.spacing.small)
        ) {
            if (!tvShowTitle.isNullOrBlank()) {
                Emphasize {
                    Text(
                        text = tvShowTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = stringResource(
                    id = R.string.episode_code,
                    episode.seasonNumber,
                    episode.episodeNumber
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall)
            )

            Text(
                text = episode.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall)
            )

            FlowRow(
                modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
            ) {
                episode.displayAirDate?.let { date ->
                    DateBadge(dateText = date, showYear = true)
                }
                ScoreBadge(score = (episode.voteAvg * 10))
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Watched Toggle Button with M3 tokens
            FilledTonalButton(
                onClick = onToggleWatched,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isWatched) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (isWatched) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                contentPadding = PaddingValues(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.small
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isWatched) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = if (isWatched) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isWatched) {
                            stringResource(id = R.string.episode_watched)
                        } else {
                            stringResource(id = R.string.mark_watched)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
