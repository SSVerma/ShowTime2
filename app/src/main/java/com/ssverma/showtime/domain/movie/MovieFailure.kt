package com.ssverma.showtime.domain.movie

sealed class MovieFailure {
    object NotFound : MovieFailure()
}