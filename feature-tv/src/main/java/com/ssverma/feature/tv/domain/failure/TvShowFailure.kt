package com.ssverma.feature.tv.domain.failure

sealed interface TvShowFailure {
    object NotFound : TvShowFailure
}

sealed interface TvSeasonFailure {
    object NotFound : TvSeasonFailure
}

sealed interface TvEpisodeFailure {
    object NotFound : TvEpisodeFailure
}