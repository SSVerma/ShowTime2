package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R

@Composable
fun TvSeasonProgressSection(
    watchedCount: Int,
    totalEpisodesCount: Int,
    isAllWatched: Boolean,
    onMarkSeasonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Text(
                    text = stringResource(id = R.string.episodes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (watchedCount > 0) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                ) {
                    Text(
                        text = "$watchedCount / $totalEpisodesCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (watchedCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.small,
                            vertical = 3.dp
                        )
                    )
                }
            }

            FilledTonalButton(
                onClick = onMarkSeasonClick,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isAllWatched) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (isAllWatched) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                contentPadding = PaddingValues(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.extraSmall
                ),
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isAllWatched) {
                            stringResource(id = R.string.season_all_watched)
                        } else {
                            stringResource(id = R.string.mark_season)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
    }
}
