package com.ssverma.showtime.domain.model

import com.ssverma.showtime.R
import com.ssverma.showtime.api.TMDB_IMAGE_BASE_URL
import com.ssverma.showtime.data.remote.response.RemoteTvShow
import com.ssverma.showtime.extension.emptyIfNull
import com.ssverma.showtime.utils.CoreUtils
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.FormatterUtils
import com.ssverma.showtime.utils.formatLocally
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

suspend fun RemoteTvShow.asTvShow(): TvShow {
    return TvShow(
        id = id,
        title = title.emptyIfNull(),
        tagline = if (tagline.isNullOrEmpty()) null else tagline,
        overview = overview ?: "",
        posterImageUrl = CoreUtils.buildImageUrl(TMDB_IMAGE_BASE_URL, posterPath),
        backdropImageUrl = CoreUtils.buildImageUrl(TMDB_IMAGE_BASE_URL, backdropPath),
        status = status.emptyIfNull(),
        voteAvg = voteAvg,
        voteAvgPercentage = voteAvg * 10f,
        voteCount = voteCount,
        displayFirstAirDate = DateUtils.parseIsoDate(firstAirDate)?.formatLocally(),
        popularity = popularity,
        displayPopularity = FormatterUtils.toRangeSymbol(popularity),
        originalLanguage = originalLanguage.emptyIfNull(),
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

suspend fun List<RemoteTvShow>.asTvShows(): List<TvShow> {
    return withContext(Dispatchers.Default) {
        map { it.asTvShow() }
    }
}

fun TvShow.imageShots(): List<ImageShot> {
    return (backdrops + posters + stills)
}

fun TvShow.topImageShots(
    max: Int = 9,
    from: List<ImageShot> = imageShots(),
): List<ImageShot> {
    return if (from.size <= max) {
        from
    } else {
        from.subList(0, max)
    }
}

fun TvShow.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.toString()
        ),
        Highlight(
            labelRes = R.string.first_air_date,
            value = displayFirstAirDate.emptyIfNull(),
        ),
        Highlight(
            labelRes = R.string.status,
            value = status
        ),
        Highlight(
            labelRes = R.string.language,
            value = originalLanguage
        ),
        Highlight(
            labelRes = R.string.seasons,
            value = seasonCount.toString()
        ),
        Highlight(
            labelRes = R.string.episode_number,
            value = episodeCount.toString()
        )
    )
}