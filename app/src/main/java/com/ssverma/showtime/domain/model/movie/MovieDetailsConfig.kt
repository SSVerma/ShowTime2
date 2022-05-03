package com.ssverma.showtime.domain.model.movie

import com.ssverma.showtime.domain.defaults.movie.MovieDefaults

data class MovieDetailsConfig(
    val movieId: Int,
    val appendable: List<MovieDetailsAppendable> = MovieDefaults.allMovieDetailsAppendable()
)

sealed interface MovieDetailsAppendable {
    object Keywords : MovieDetailsAppendable
    object Credits : MovieDetailsAppendable
    object Images : MovieDetailsAppendable
    object Videos : MovieDetailsAppendable
    object Lists : MovieDetailsAppendable
    object Reviews : MovieDetailsAppendable
    object Similar : MovieDetailsAppendable
    object Recommendations : MovieDetailsAppendable
}