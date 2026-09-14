package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R

@Composable
fun TvEpisodeTimelineNavSection(
    currentEpisodeNumber: Int,
    onPreviousEpisodeClick: () -> Unit,
    onNextEpisodeClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasPrevious: Boolean = currentEpisodeNumber > 1,
    hasNext: Boolean = true
) {
    if (!hasPrevious && !hasNext) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        if (hasPrevious && hasNext) {
            OutlinedButton(
                onClick = onPreviousEpisodeClick,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.previous_episode),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            OutlinedButton(
                onClick = onNextEpisodeClick,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    Text(
                        text = stringResource(id = R.string.next_episode),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else if (hasPrevious) {
            OutlinedButton(
                onClick = onPreviousEpisodeClick,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.previous_episode),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        } else {
            OutlinedButton(
                onClick = onNextEpisodeClick,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    Text(
                        text = stringResource(id = R.string.next_episode),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
