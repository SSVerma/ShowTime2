package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.RemoteTvSeason
import com.ssverma.showtime.domain.model.*
import com.ssverma.showtime.extension.emptyIfNull
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatLocally
import javax.inject.Inject

class TvSeasonMapper @Inject constructor() : Mapper<RemoteTvSeason, TvSeason>() {
    override suspend fun map(input: RemoteTvSeason): TvSeason {
        return input.asTvSeason()
    }
}

class TvSeasonsMapper @Inject constructor() : ListMapper<RemoteTvSeason, TvSeason>() {
    override suspend fun mapItem(input: RemoteTvSeason): TvSeason {
        return input.asTvSeason()
    }
}

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