package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader

@Composable
fun <T, FF> AppSection(
    title: String,
    uiState: UiState<List<T>, FF>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onTrailingActionClicked: () -> Unit = {},
    onRetry: () -> Unit,
    loadingPlaceholder: @Composable () -> Unit,
    isVertical: Boolean = false,
    content: @Composable (T) -> Unit
) {
    Section(
        modifier = modifier.padding(top = 32.dp),
        sectionHeader = {
            SectionHeader(
                modifier = Modifier.padding(start = 16.dp),
                title = title,
                subtitle = subtitle,
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
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                        items.take(5).forEach { content(it) }
                    }
                } else {
                    HorizontalLazyList(items = items) { content(it) }
                }
            }
        }
    }
}
