package com.ssverma.core.ui.layout

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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