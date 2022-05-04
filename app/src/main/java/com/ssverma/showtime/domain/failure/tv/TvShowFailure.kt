package com.ssverma.showtime.domain.failure.tv

sealed interface TvShowFailure {
    object NotFound : TvShowFailure
}

sealed interface TvSeasonFailure {
    object NotFound : TvSeasonFailure
}

sealed interface TvEpisodeFailure {
    object NotFound : TvEpisodeFailure
}