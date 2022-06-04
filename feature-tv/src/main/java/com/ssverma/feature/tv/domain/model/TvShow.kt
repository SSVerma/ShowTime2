package com.ssverma.feature.tv.domain.model

import com.ssverma.core.domain.model.*

class TvShow(
    val id: Int,
    val title: String,
    val tagline: String?,
    val overview: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val status: String,
    val voteAvg: Float,
    val voteAvgPercentage: Float,
    val voteCount: Int,
    val displayFirstAirDate: String?,
    val popularity: Float,
    val displayPopularity: String,
    val originalLanguage: String,
    val seasonCount: Int,
    val episodeCount: Int,
    val casts: List<Cast>,
    val guestStars: List<Cast>,
    val crews: List<Crew>,
    val keywords: List<Keyword>,
    val posters: List<ImageShot>,
    val backdrops: List<ImageShot>,
    val stills: List<ImageShot>,
    val videos: List<Video>,
    val generes: List<Genre>,
    val reviews: List<Review>,
    val similarTvShows: List<TvShow>,
    val recommendations: List<TvShow>,
    val seasons: List<TvSeason>
)

fun TvShow.imageShots(): List<ImageShot> {
    return (backdrops + posters + stills)
}