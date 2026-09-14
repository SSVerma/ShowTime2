package com.ssverma.feature.tv.navigation.args

data class TvEpisodeArgs(
    val tvShowId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val tvShowTitle: String? = null,
    val tvShowPosterPath: String? = null,
    val tvShowBackdropPath: String? = null
)