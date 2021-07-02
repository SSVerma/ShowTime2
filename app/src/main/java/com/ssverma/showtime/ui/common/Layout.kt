package com.ssverma.showtime.ui.common

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import kotlin.math.ceil
import kotlin.math.min

@Composable
fun <T> VerticalGrid(
    items: List<T>,
    @IntRange(from = 1) columnCount: Int,
    modifier: Modifier = Modifier,
    max: Int = items.size,
    verticalArrangement: Arrangement.Vertical = Arrangement.SpaceEvenly,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceEvenly,
    itemContent: @Composable (index: Int, item: T) -> Unit
) {
    val rowCount = ceil(min(max, items.size) / columnCount.toDouble()).toInt()

    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        for (rowIndex in 0 until rowCount) {
            Row(
                horizontalArrangement = horizontalArrangement,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (columnIndex in 0 until columnCount) {
                    val itemIndex = rowIndex * columnCount + columnIndex
                    if (itemIndex < items.size) {
                        Box(
                            modifier = Modifier.weight(1f, fill = true),
                            propagateMinConstraints = true
                        ) {
                            itemContent(itemIndex, items[itemIndex])
                        }
                    } else {
                        Spacer(Modifier.weight(1f, fill = true))
                    }
                }
            }
        }

    }
}

@Composable
fun <T> StaggeredVerticalGrid(
    items: List<T>,
    maxColumnWidth: Dp,
    modifier: Modifier = Modifier,
    itemContent: @Composable (index: Int, item: T) -> Unit
) {
    StaggeredLayout(maxColumnWidth = maxColumnWidth, modifier = modifier) {
        items.forEachIndexed { index, item ->
            itemContent(index, item)
        }
    }
}


/*
* Snippet from Owl sample
*
* */
@Composable
fun StaggeredLayout(
    modifier: Modifier = Modifier,
    maxColumnWidth: Dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        check(constraints.hasBoundedWidth) {
            "Unbounded width not supported"
        }
        val columns = ceil(constraints.maxWidth / maxColumnWidth.toPx()).toInt()
        val columnWidth = constraints.maxWidth / columns
        val itemConstraints = constraints.copy(maxWidth = columnWidth)
        val colHeights = IntArray(columns) { 0 } // track each column's height
        val placeables = measurables.map { measurable ->
            val column = shortestColumn(colHeights)
            val placeable = measurable.measure(itemConstraints)
            colHeights[column] += placeable.height
            placeable
        }

        val height = colHeights.maxOrNull()?.coerceIn(constraints.minHeight, constraints.maxHeight)
            ?: constraints.minHeight
        layout(
            width = constraints.maxWidth,
            height = height
        ) {
            val colY = IntArray(columns) { 0 }
            placeables.forEach { placeable ->
                val column = shortestColumn(colY)
                placeable.place(
                    x = columnWidth * column,
                    y = colY[column]
                )
                colY[column] += placeable.height
            }
        }
    }
}

private fun shortestColumn(colHeights: IntArray): Int {
    var minHeight = Int.MAX_VALUE
    var column = 0
    colHeights.forEachIndexed { index, height ->
        if (height < minHeight) {
            minHeight = height
            column = index
        }
    }
    return column
}