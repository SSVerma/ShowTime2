package com.ssverma.showtime.ui.movie

import androidx.compose.runtime.Composable
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.ui.common.Chip

@Composable
fun GenreItem(genre: Genre) {
    Chip(text = genre.name) {
        //
    }
}