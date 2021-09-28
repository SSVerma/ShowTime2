package com.ssverma.showtime.domain.model

import com.ssverma.showtime.R
import com.ssverma.showtime.api.convertToFullTmdbImageUrl
import com.ssverma.showtime.data.remote.response.RemoteTvSeason
import com.ssverma.showtime.extension.emptyIfNull
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatLocally

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

suspend fun RemoteTvSeason.asTvSeason(): TvSeason {
    return TvSeason(
        id = id,
        title = title.emptyIfNull(),
        posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
        overview = overview.emptyIfNull(),
        displayAirDate = DateUtils.parseIsoDate(airDate)?.formatLocally(),
        seasonNumber = seasonNumber,
        episodeCount = episodeCount,
        episodes = episodes?.asTvEpisodes() ?: emptyList(),
        casts = credit?.casts?.asCasts() ?: emptyList(),
        crews = credit?.crews?.asCrews() ?: emptyList(),
        posters = imagePayload?.posters?.asImagesShots() ?: emptyList(),
        videos = videoPayload?.videos?.asVideos() ?: emptyList()
    )
}

suspend fun List<RemoteTvSeason>.asTvSeasons() = map { it.asTvSeason() }

fun TvSeason.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.season_number,
            value = seasonNumber.toString()
        ),
        Highlight(
            labelRes = R.string.episodes,
            value = episodes.size.toString()
        ),
        Highlight(
            labelRes = R.string.first_air_date,
            value = displayAirDate.emptyIfNull()
        ),
    )
}