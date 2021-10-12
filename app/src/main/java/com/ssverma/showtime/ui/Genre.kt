package com.ssverma.showtime.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.ui.common.Chip

@Composable
fun GenreItem(genre: Genre, onGenreClicked: () -> Unit) {
    Chip(
        shape = RoundedCornerShape(50),
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.24f),
        modifier = Modifier
            .clickable {
                onGenreClicked()
            }
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.87f)
            )
    ) {
        Text(
            text = genre.name,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface
        )
    }
}