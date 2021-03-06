package com.ssverma.shared.domain.model.tv

import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.Crew
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.Video

data class TvEpisode(
    val id: Int,
    val title: String,
    val posterImageUrl: String,
    val overview: String,
    val displayAirDate: String?,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val voteAvg: Float,
    val voteCount: Int,
    val casts: List<Cast>,
    val guestStars: List<Cast>,
    val crews: List<Crew>,
    val stills: List<ImageShot>,
    val videos: List<Video>
)