package com.ssverma.feature.movie.domain.failure

sealed class MovieFailure {
    object NotFound : MovieFailure()
}