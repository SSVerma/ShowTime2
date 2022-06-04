package com.ssverma.feature.tv.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.RemoteTvEpisode
import com.ssverma.core.domain.utils.DateUtils
import com.ssverma.core.domain.utils.formatLocally
import com.ssverma.feature.tv.domain.model.TvEpisode
import com.ssverma.shared.data.mapper.*
import javax.inject.Inject

class TvEpisodeMapper @Inject constructor() : Mapper<RemoteTvEpisode, TvEpisode>() {
    override suspend fun map(input: RemoteTvEpisode): TvEpisode {
        return input.asTvEpisode()
    }
}

class TvEpisodesMapper @Inject constructor() : ListMapper<RemoteTvEpisode, TvEpisode>() {
    override suspend fun mapItem(input: RemoteTvEpisode): TvEpisode {
        return input.asTvEpisode()
    }
}

suspend fun RemoteTvEpisode.asTvEpisode(): TvEpisode {
    return TvEpisode(
        id = id,
        title = title.orEmpty(),
        posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
        overview = overview.orEmpty(),
        displayAirDate = DateUtils.parseIsoDate(airDate)?.formatLocally(),
        episodeNumber = episodeNumber,
        seasonNumber = seasonNumber,
        voteAvg = voteAvg,
        voteCount = voteCount,
        casts = credit?.casts?.asCasts() ?: emptyList(),
        guestStars = credit?.guestStars?.asCasts() ?: emptyList(),
        crews = credit?.crews?.asCrews() ?: emptyList(),
        stills = imagePayload?.stills?.asImagesShots() ?: emptyList(),
        videos = videoPayload?.videos?.asVideos() ?: emptyList()
    )
}

suspend fun List<RemoteTvEpisode>.asTvEpisodes() = map { it.asTvEpisode() }