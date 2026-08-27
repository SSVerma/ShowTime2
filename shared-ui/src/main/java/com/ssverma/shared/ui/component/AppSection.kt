package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.ui.component.media.SeeAllCard

@Composable
fun <T, FF> AppSection(
    title: String,
    uiState: UiState<List<T>, FF>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onTrailingActionClicked: () -> Unit = {},
    showTrailingActionHeader: Boolean = true,
    showTrailingActionCard: Boolean = false,
    onRetry: () -> Unit,
    loadingPlaceholder: @Composable () -> Unit,
    isVertical: Boolean = false,
    maxVerticalItems: Int = 5,
    content: @Composable (T) -> Unit
) {
    Section(
        modifier = modifier,
        sectionHeader = {
            SectionHeader(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = title,
                subtitle = subtitle,
                hideTrailingAction = !showTrailingActionHeader,
                onTrailingActionClicked = onTrailingActionClicked
            )
        }
    ) {
        DriveCompose(
            uiState = uiState,
            loading = {
                if (isVertical) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(3) { loadingPlaceholder() }
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(3) { loadingPlaceholder() }
                    }
                }
            },
            onRetry = onRetry
        ) { items ->
            if (items.isNotEmpty()) {
                if (isVertical) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val limit = minOf(maxVerticalItems, items.size)
                        for (i in 0 until limit) {
                            content(items[i])
                        }
                    }
                } else {
                    if (showTrailingActionCard) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items) {
                                content(it)
                            }
                            item {
                                SeeAllCard(onClick = onTrailingActionClicked)
                            }
                        }
                    } else {
                        HorizontalLazyList(items = items) { content(it) }
                    }
                }
            }
        }
    }
}
