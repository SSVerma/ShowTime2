package com.ssverma.feature.library.ui.diary.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.model.diary.DiaryFilterType

@Composable
fun DiaryFilterRow(
    activeFilter: DiaryFilterType,
    onFilterSelected: (DiaryFilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterScrollState = rememberScrollState()

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(filterScrollState)
            .padding(vertical = 4.dp)
    ) {
        DiaryFilterType.entries.forEach { filter ->
            val labelRes = when (filter) {
                DiaryFilterType.ALL -> R.string.filter_all
                DiaryFilterType.MOVIES_ONLY -> R.string.filter_movies
                DiaryFilterType.TV_ONLY -> R.string.filter_tv_shows
                DiaryFilterType.REWATCHES_ONLY -> R.string.diary_filter_rewatches
                DiaryFilterType.FIVE_STARS_ONLY -> R.string.diary_filter_five_stars
            }

            val icon = when (filter) {
                DiaryFilterType.ALL -> Icons.Rounded.AutoAwesome
                DiaryFilterType.MOVIES_ONLY -> Icons.Rounded.Movie
                DiaryFilterType.TV_ONLY -> Icons.Rounded.Tv
                DiaryFilterType.REWATCHES_ONLY -> Icons.Rounded.Replay
                DiaryFilterType.FIVE_STARS_ONLY -> Icons.Rounded.Star
            }

            val isSelected = activeFilter == filter

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = stringResource(labelRes),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
