package com.ssverma.feature.library.ui.backlog.detail.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.R

@Composable
fun ChallengeFilterRow(
    totalCount: Int,
    remainingCount: Int,
    watchedCount: Int,
    selectedIndex: Int,
    onFilterSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = MaterialTheme.spacing.mediumLarge)
    ) {
        val isAll = selectedIndex == 0
        FilterChip(
            selected = isAll,
            onClick = { onFilterSelect(0) },
            label = {
                Text(
                    text = stringResource(R.string.challenges_filter_all_count, totalCount),
                    maxLines = 1,
                    softWrap = false
                )
            },
            shape = CircleShape,
            colors = chipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isAll,
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                selectedBorderColor = Color.Transparent
            )
        )

        val isRemaining = selectedIndex == 1
        FilterChip(
            selected = isRemaining,
            onClick = { onFilterSelect(1) },
            label = {
                Text(
                    text = stringResource(
                        R.string.challenges_filter_remaining_count,
                        remainingCount
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            },
            shape = CircleShape,
            colors = chipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isRemaining,
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                selectedBorderColor = Color.Transparent
            )
        )

        val isWatched = selectedIndex == 2
        FilterChip(
            selected = isWatched,
            onClick = { onFilterSelect(2) },
            label = {
                Text(
                    text = stringResource(R.string.challenges_filter_watched_count, watchedCount),
                    maxLines = 1,
                    softWrap = false
                )
            },
            shape = CircleShape,
            colors = chipColors,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isWatched,
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                selectedBorderColor = Color.Transparent
            )
        )
    }
}
