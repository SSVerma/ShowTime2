package com.ssverma.shared.ui.component.community

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.community.ThreadFilter
import com.ssverma.shared.ui.R

@Composable
fun DiscussionFilterBar(
    selectedFilter: ThreadFilter,
    onFilterSelected: (ThreadFilter) -> Unit,
    isScrolled: Boolean,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = if (isScrolled) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "DiscussionFilterElevation"
    )

    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = elevation,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == ThreadFilter.ALL,
                onClick = { onFilterSelected(ThreadFilter.ALL) },
                label = {
                    Text(
                        text = stringResource(id = R.string.filter_newest),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedFilter == ThreadFilter.ALL) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = if (selectedFilter == ThreadFilter.ALL) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(size = FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )

            FilterChip(
                selected = selectedFilter == ThreadFilter.TOP_UPVOTED,
                onClick = { onFilterSelected(ThreadFilter.TOP_UPVOTED) },
                label = {
                    Text(
                        text = stringResource(id = R.string.filter_top_upvoted),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedFilter == ThreadFilter.TOP_UPVOTED) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = if (selectedFilter == ThreadFilter.TOP_UPVOTED) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(size = FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )

            FilterChip(
                selected = selectedFilter == ThreadFilter.SPOILER_FREE,
                onClick = { onFilterSelected(ThreadFilter.SPOILER_FREE) },
                label = {
                    Text(
                        text = stringResource(id = R.string.filter_spoiler_free),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedFilter == ThreadFilter.SPOILER_FREE) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = if (selectedFilter == ThreadFilter.SPOILER_FREE) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(size = FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}
