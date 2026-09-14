package com.ssverma.feature.tv.navigation.args

data class TvSeasonArgs(
    val tvShowId: Int,
    val seasonNumber: Int,
    val tvShowTitle: String? = null,
    val tvShowPosterPath: String? = null,
    val tvShowBackdropPath: String? = null
)