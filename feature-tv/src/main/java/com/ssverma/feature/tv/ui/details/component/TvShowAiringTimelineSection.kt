package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.tv.TvEpisodePreview
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionContentHeaderSpacing

@Composable
fun TvShowAiringTimelineSection(
    nextEpisodeToAir: TvEpisodePreview?,
    lastEpisodeToAir: TvEpisodePreview?,
    modifier: Modifier = Modifier
) {
    if (nextEpisodeToAir == null && lastEpisodeToAir == null) return

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.airing_timeline),
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                hideTrailingAction = true
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        modifier = modifier
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            ) {
                nextEpisodeToAir?.let { nextEp ->
                    AiringTimelineRow(
                        badgeLabel = stringResource(id = R.string.next_episode_to_air),
                        episodeCode = stringResource(
                            id = R.string.episode_code,
                            nextEp.seasonNumber,
                            nextEp.episodeNumber
                        ),
                        episodeTitle = nextEp.title,
                        displayDate = nextEp.displayAirDate,
                        icon = Icons.Rounded.CalendarToday,
                        isUpcoming = true
                    )
                }

                if (nextEpisodeToAir != null && lastEpisodeToAir != null) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
                    )
                }

                lastEpisodeToAir?.let { lastEp ->
                    AiringTimelineRow(
                        badgeLabel = stringResource(id = R.string.last_episode_to_air),
                        episodeCode = stringResource(
                            id = R.string.episode_code,
                            lastEp.seasonNumber,
                            lastEp.episodeNumber
                        ),
                        episodeTitle = lastEp.title,
                        displayDate = lastEp.displayAirDate,
                        icon = Icons.Rounded.History,
                        isUpcoming = false
                    )
                }
            }
        }
    }
}

@Composable
private fun AiringTimelineRow(
    badgeLabel: String,
    episodeCode: String,
    episodeTitle: String,
    displayDate: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isUpcoming: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (isUpcoming) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUpcoming) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .padding(MaterialTheme.spacing.small)
                    .size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
            ) {
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUpcoming) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = episodeCode,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = episodeTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            displayDate?.let { date ->
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
