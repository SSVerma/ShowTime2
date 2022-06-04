package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.TMDB_IMAGE_BASE_URL
import com.ssverma.api.service.tmdb.response.RemoteTvShow
import com.ssverma.shared.domain.utils.CoreUtils
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.FormatterUtils
import com.ssverma.shared.domain.utils.formatLocally
import com.ssverma.shared.data.mapper.*
import com.ssverma.feature.tv.domain.model.TvShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TvShowMapper @Inject constructor() : Mapper<RemoteTvShow, com.ssverma.feature.tv.domain.model.TvShow>() {
    override suspend fun map(input: RemoteTvShow): com.ssverma.feature.tv.domain.model.TvShow {
        return input.asTvShow()
    }
}

class TvShowsMapper @Inject constructor() : ListMapper<RemoteTvShow, com.ssverma.feature.tv.domain.model.TvShow>() {
    override suspend fun mapItem(input: RemoteTvShow): com.ssverma.feature.tv.domain.model.TvShow {
        return input.asTvShow()
    }
}

suspend fun RemoteTvShow.asTvShow(): com.ssverma.feature.tv.domain.model.TvShow {
    return com.ssverma.feature.tv.domain.model.TvShow(
        id = id,
        title = title.orEmpty(),
        tagline = if (tagline.isNullOrEmpty()) null else tagline,
        overview = overview.orEmpty(),
        posterImageUrl = CoreUtils.buildImageUrl(TMDB_IMAGE_BASE_URL, posterPath),
        backdropImageUrl = CoreUtils.buildImageUrl(TMDB_IMAGE_BASE_URL, backdropPath),
        status = status.orEmpty(),
        voteAvg = voteAvg,
        voteAvgPercentage = voteAvg * 10f,
        voteCount = voteCount,
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
        videos = videoPayload?.videos?.filterYoutubeVideos() ?: emptyList(),
        generes = genres?.asGenres() ?: emptyList(),
        reviews = reviews?.results?.asReviews()?.asReversed() ?: emptyList(),
        similarTvShows = similarTvShows?.results?.asTvShows() ?: emptyList(),
        recommendations = recommendations?.results?.asTvShows() ?: emptyList(),
        seasons = seasons?.asTvSeasons()?.reversed() ?: emptyList()
    )
}

suspend fun List<RemoteTvShow>.asTvShows(): List<com.ssverma.feature.tv.domain.model.TvShow> {
    return withContext(Dispatchers.Default) {
        map { it.asTvShow() }
    }
}