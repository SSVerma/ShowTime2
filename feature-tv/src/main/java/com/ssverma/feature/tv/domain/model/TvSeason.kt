package com.ssverma.feature.tv.domain.model

import com.ssverma.core.domain.model.ImageShot
import com.ssverma.core.domain.model.Cast
import com.ssverma.core.domain.model.Crew
import com.ssverma.core.domain.model.Video

data class TvSeason(
    val id: Int,
    val title: String,
    val posterImageUrl: String,
    val overview: String,
    val displayAirDate: String?,
    val seasonNumber: Int,
    val episodeCount: Int,
    val episodes: List<TvEpisode>,
    val casts: List<Cast>,
    val crews: List<Crew>,
    val posters: List<ImageShot>,
    val videos: List<Video>
)