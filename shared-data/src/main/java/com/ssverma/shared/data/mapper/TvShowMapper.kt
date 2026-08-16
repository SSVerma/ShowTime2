package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.api.service.tmdb.response.RemoteTvShow
import com.ssverma.shared.domain.model.primaryTrailer
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.FormatterUtils
import com.ssverma.shared.domain.utils.formatLocally
import javax.inject.Inject

class TvShowMapper @Inject constructor() : Mapper<RemoteTvShow, TvShow>() {
    override suspend fun map(input: RemoteTvShow): TvShow {
        return input.asTvShow()
    }
}

class TvShowsMapper @Inject constructor() : ListMapper<RemoteTvShow, TvShow>() {
    override suspend fun mapItem(input: RemoteTvShow): TvShow {
        return input.asTvShow()
    }
}

private suspend fun RemoteTvShow.asTvShow(): TvShow {
    val youtubeVideos = videoPayload?.videos?.filterYoutubeVideos() ?: emptyList()

    return TvShow(
        id = id,
        title = title.orEmpty(),
        tagline = if (tagline.isNullOrEmpty()) null else tagline,
        overview = overview.orEmpty(),
        posterImageUrl = posterPath.convertToTmdbPosterUrl(),
        backdropImageUrl = backdropPath.convertToTmdbBackdropUrl(),
        status = status.orEmpty(),
        voteAvg = voteAvg,
        voteAvgPercentage = voteAvg * 10f,
        voteCount = voteCount,
        firstAirDate = DateUtils.parseIsoDate(firstAirDate),
        displayFirstAirDate = DateUtils.parseIsoDate(firstAirDate)?.formatLocally(),
        popularity = popularity,
        displayPopularity = FormatterUtils.toRangeSymbol(popularity),
        originalLanguage = originalLanguage.orEmpty(),
        seasonCount = seasonCount,
        episodeCount = episodeCount,
        casts = credit?.casts?.asCasts() ?: emptyList(),
        guestStars = credit?.guestStars?.asCasts() ?: emptyList(),
        crews = credit?.crews?.asCrews() ?: emptyList(),
        keywords = keywordPayload?.keywords?.asKeywords() ?: emptyList(),
        posters = imagePayload?.posters?.asImagesShots() ?: emptyList(),
        backdrops = imagePayload?.backdrops?.asImagesShots() ?: emptyList(),
        stills = imagePayload?.stills?.asImagesShots() ?: emptyList(),
        videos = youtubeVideos,
        primaryTrailer = youtubeVideos.primaryTrailer(),
        generes = genres?.asGenres() ?: emptyList(),
        reviews = reviews?.results?.asReviews()?.asReversed() ?: emptyList(),
        similarTvShows = similarTvShows?.results?.asTvShows() ?: emptyList(),
        recommendations = recommendations?.results?.asTvShows() ?: emptyList(),
        seasons = seasons?.asTvSeasons()?.reversed() ?: emptyList(),
        watchProviders = watchProviders?.asWatchProvidersMap() ?: emptyMap(),
    )
}

private suspend fun List<RemoteTvShow>.asTvShows(): List<TvShow> {
    return map { it.asTvShow() }
}
