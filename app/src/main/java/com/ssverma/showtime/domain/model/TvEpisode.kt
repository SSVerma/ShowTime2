package com.ssverma.showtime.domain.model

import com.ssverma.showtime.R
import com.ssverma.showtime.api.convertToFullTmdbImageUrl
import com.ssverma.showtime.data.remote.response.RemoteTvEpisode
import com.ssverma.showtime.extension.emptyIfNull
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatLocally

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
    val posters: List<ImageShot>,
    val videos: List<Video>
)

suspend fun RemoteTvEpisode.asTvEpisode(): TvEpisode {
    return TvEpisode(
        id = id,
        title = title.emptyIfNull(),
        posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
        overview = overview.emptyIfNull(),
        displayAirDate = DateUtils.parseIsoDate(airDate)?.formatLocally(),
        episodeNumber = episodeNumber,
        seasonNumber = seasonNumber,
        voteAvg = voteAvg,
        voteCount = voteCount,
        casts = credit?.casts?.asCasts() ?: emptyList(),
        guestStars = credit?.guestStars?.asCasts() ?: emptyList(),
        crews = credit?.crews?.asCrews() ?: emptyList(),
        posters = imagePayload?.posters?.asImagesShots() ?: emptyList(),
        videos = videoPayload?.videos?.asVideos() ?: emptyList()
    )
}

suspend fun List<RemoteTvEpisode>.asTvEpisodes() = map { it.asTvEpisode() }

fun TvEpisode.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.season_number,
            value = seasonNumber.toString()
        ),
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.toString()
        ),
        Highlight(
            labelRes = R.string.air_date,
            value = displayAirDate.emptyIfNull()
        ),
    )
}