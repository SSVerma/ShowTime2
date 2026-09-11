package com.ssverma.shared.domain.model.tv

import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.Crew
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.Keyword
import com.ssverma.shared.domain.model.Review
import com.ssverma.shared.domain.model.Video
import com.ssverma.shared.domain.model.WatchProvider
import java.time.LocalDate

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
    val firstAirDate: LocalDate?,
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
    val primaryTrailer: Video? = null,
    val generes: List<Genre>,
    val reviews: List<Review>,
    val similarTvShows: List<TvShow>,
    val recommendations: List<TvShow>,
    val seasons: List<TvSeason>,
    val watchProviders: Map<String, WatchProvider>,
    val nextEpisodeToAir: TvEpisodePreview? = null,
    val lastEpisodeToAir: TvEpisodePreview? = null
) {
    /** Whether the TV show has scheduled future episodes to air */
    val hasUpcomingEpisodes: Boolean
        get() {
            val nextEpisodeDate = nextEpisodeToAir?.airDate
            return nextEpisodeDate != null && !nextEpisodeDate.isBefore(LocalDate.now())
        }

    /** Whether the TV series has not yet premiered */
    val isUpcoming: Boolean
        get() {
            val today = LocalDate.now()
            if (firstAirDate != null && firstAirDate.isAfter(today)) return true
            return status.equals("In Production", ignoreCase = true) ||
                    status.equals("Planned", ignoreCase = true)
        }
}

fun TvShow.imageShots(): List<ImageShot> {
    return (backdrops + posters + stills)
}