package com.ssverma.showtime.domain.model.movie

import com.ssverma.showtime.domain.defaults.movie.MovieDefaults
import com.ssverma.showtime.domain.model.MediaDetailsAppendable

data class MovieDetailsConfig(
    val movieId: Int,
    val appendable: List<MediaDetailsAppendable> = MovieDefaults.allMovieDetailsAppendable()
)