package com.ssverma.showtime.domain.failure.movie

sealed class MovieFailure {
    object NotFound : MovieFailure()
}