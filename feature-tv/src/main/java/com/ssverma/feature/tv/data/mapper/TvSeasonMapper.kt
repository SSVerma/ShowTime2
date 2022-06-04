package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.RemoteTvSeason
import com.ssverma.core.domain.utils.DateUtils
import com.ssverma.core.domain.utils.formatLocally
import com.ssverma.feature.tv.data.mapper.asTvEpisodes
import com.ssverma.shared.data.mapper.*
import javax.inject.Inject

class TvSeasonMapper @Inject constructor() : Mapper<RemoteTvSeason, com.ssverma.feature.tv.domain.model.TvSeason>() {
    override suspend fun map(input: RemoteTvSeason): com.ssverma.feature.tv.domain.model.TvSeason {
        return input.asTvSeason()
    }
}

class TvSeasonsMapper @Inject constructor() : ListMapper<RemoteTvSeason, com.ssverma.feature.tv.domain.model.TvSeason>() {
    override suspend fun mapItem(input: RemoteTvSeason): com.ssverma.feature.tv.domain.model.TvSeason {
        return input.asTvSeason()
    }
}

suspend fun RemoteTvSeason.asTvSeason(): com.ssverma.feature.tv.domain.model.TvSeason {
    return com.ssverma.feature.tv.domain.model.TvSeason(
        id = id,
        title = title.orEmpty(),
        posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
        overview = overview.orEmpty(),
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