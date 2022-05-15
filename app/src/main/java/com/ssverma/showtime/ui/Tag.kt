package com.ssverma.showtime.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.domain.model.Keyword

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TagItem(keyword: Keyword, onTagClicked: () -> Unit) {
    Chip(
        shape = MaterialTheme.shapes.small.copy(CornerSize(8.dp)),
        colors = ChipDefaults.outlinedChipColors(
            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.24f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.87f)
        ),
        onClick = onTagClicked,
    ) {
        Text(
            text = "#${keyword.name}",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface
        )
    }
}