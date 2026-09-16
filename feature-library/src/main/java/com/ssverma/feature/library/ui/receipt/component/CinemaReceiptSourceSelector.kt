package com.ssverma.feature.library.ui.receipt.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.R
import com.ssverma.feature.library.domain.model.ReceiptSource
import com.ssverma.shared.domain.model.library.CustomList

@Composable
fun CinemaReceiptSourceSelector(
    selectedSource: ReceiptSource,
    selectedCustomList: CustomList?,
    customLists: List<CustomList>,
    onSourceSelected: (ReceiptSource) -> Unit,
    onCustomListSelected: (CustomList) -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.receipt_source),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            val isHistory = selectedSource == ReceiptSource.HISTORY && selectedCustomList == null
            FilterChip(
                selected = isHistory,
                onClick = { onSourceSelected(ReceiptSource.HISTORY) },
                label = {
                    Text(
                        text = stringResource(R.string.receipt_period_history),
                        fontWeight = if (isHistory) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = CircleShape,
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isHistory,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    selectedBorderColor = Color.Transparent
                )
            )

            val isThisMonth =
                selectedSource == ReceiptSource.THIS_MONTH && selectedCustomList == null
            FilterChip(
                selected = isThisMonth,
                onClick = { onSourceSelected(ReceiptSource.THIS_MONTH) },
                label = {
                    Text(
                        text = stringResource(R.string.receipt_period_this_month),
                        fontWeight = if (isThisMonth) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = CircleShape,
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isThisMonth,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    selectedBorderColor = Color.Transparent
                )
            )

            val isThisYear = selectedSource == ReceiptSource.THIS_YEAR && selectedCustomList == null
            FilterChip(
                selected = isThisYear,
                onClick = { onSourceSelected(ReceiptSource.THIS_YEAR) },
                label = {
                    Text(
                        text = stringResource(R.string.receipt_period_this_year),
                        fontWeight = if (isThisYear) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = CircleShape,
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isThisYear,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    selectedBorderColor = Color.Transparent
                )
            )

            val isLast90Days =
                selectedSource == ReceiptSource.LAST_90_DAYS && selectedCustomList == null
            FilterChip(
                selected = isLast90Days,
                onClick = { onSourceSelected(ReceiptSource.LAST_90_DAYS) },
                label = {
                    Text(
                        text = stringResource(R.string.receipt_period_last_90_days),
                        fontWeight = if (isLast90Days) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Update,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = CircleShape,
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isLast90Days,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    selectedBorderColor = Color.Transparent
                )
            )

            val isFavorites =
                selectedSource == ReceiptSource.FAVORITES && selectedCustomList == null
            FilterChip(
                selected = isFavorites,
                onClick = { onSourceSelected(ReceiptSource.FAVORITES) },
                label = {
                    Text(
                        text = stringResource(R.string.receipt_period_favorites),
                        fontWeight = if (isFavorites) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = CircleShape,
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isFavorites,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    selectedBorderColor = Color.Transparent
                )
            )

            val isWatchlist =
                selectedSource == ReceiptSource.WATCHLIST && selectedCustomList == null
            FilterChip(
                selected = isWatchlist,
                onClick = { onSourceSelected(ReceiptSource.WATCHLIST) },
                label = {
                    Text(
                        text = stringResource(R.string.receipt_period_watchlist),
                        fontWeight = if (isWatchlist) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = CircleShape,
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isWatchlist,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    selectedBorderColor = Color.Transparent
                )
            )

            customLists.forEach { customList ->
                val isCustomSelected = selectedCustomList?.listId == customList.listId
                FilterChip(
                    selected = isCustomSelected,
                    onClick = { onCustomListSelected(customList) },
                    label = {
                        Text(
                            text = customList.title,
                            fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.List,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = CircleShape,
                    colors = chipColors,
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isCustomSelected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}
