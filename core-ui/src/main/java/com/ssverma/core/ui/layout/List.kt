package com.ssverma.core.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun <T> HorizontalLazyList(
    items: List<T>,
    contentPadding: PaddingValues = SectionListContentPadding,
    horizontalArrangement: Arrangement.Horizontal = SectionListHorizontalArrangement,
    itemContent: @Composable (item: T) -> Unit
) {
    HorizontalLazyListIndexed(
        items = items,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
    ) { _, item ->
        itemContent(item)
    }
}

@Composable
fun <T> HorizontalLazyListIndexed(
    items: List<T>,
    contentPadding: PaddingValues = SectionListContentPadding,
    horizontalArrangement: Arrangement.Horizontal = SectionListHorizontalArrangement,
    itemContent: @Composable (index: Int, item: T) -> Unit
) {
    LazyRow(
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        content = {
            itemsIndexed(items) { index, item ->
                itemContent(index, item)
            }
        }
    )
}

private val SectionListContentPadding = PaddingValues(start = 16.dp, end = 16.dp)
private val SectionListHorizontalArrangement = Arrangement.spacedBy(16.dp)