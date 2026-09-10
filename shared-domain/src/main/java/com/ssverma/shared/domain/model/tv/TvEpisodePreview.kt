package com.ssverma.shared.domain.model.tv

import java.time.LocalDate

data class TvEpisodePreview(
    val id: Int,
    val title: String,
    val airDate: LocalDate?,
    val displayAirDate: String?,
    val seasonNumber: Int,
    val episodeNumber: Int
)
