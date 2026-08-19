package com.ssverma.feature.movie.domain.model

import com.ssverma.feature.movie.domain.defaults.MovieDefaults
import com.ssverma.shared.domain.model.MediaDetailsAppendable

data class MovieDetailsConfig(
    val movieId: Int,
    val appendable: List<MediaDetailsAppendable> = MovieDefaults.allMovieDetailsAppendable()
)