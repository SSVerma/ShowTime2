package com.ssverma.feature.movie.domain.model

import com.ssverma.shared.domain.model.MediaDetailsAppendable
import com.ssverma.feature.movie.domain.defaults.MovieDefaults

data class MovieDetailsConfig(
    val movieId: Int,
    val appendable: List<MediaDetailsAppendable> = MovieDefaults.allMovieDetailsAppendable()
)