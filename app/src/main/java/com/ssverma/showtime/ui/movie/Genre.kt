package com.ssverma.showtime.ui.movie

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.ui.common.Chip

@Composable
fun GenreItem(genre: Genre) {
    Chip(
        shape = RoundedCornerShape(50),
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.32f)
    ) {
        Text(text = genre.name)
    }
}