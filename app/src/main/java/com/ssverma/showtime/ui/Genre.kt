package com.ssverma.showtime.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.domain.model.Genre

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GenreItem(genre: Genre, onGenreClicked: () -> Unit) {
    Chip(
        shape = RoundedCornerShape(50),
        colors = ChipDefaults.outlinedChipColors(
            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.24f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.87f)
        ),
        onClick = onGenreClicked
    ) {
        Text(
            text = genre.name,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface
        )
    }
}