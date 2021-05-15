package com.ssverma.showtime.domain.model

import com.ssverma.showtime.api.TMDB_IMAGE_BASE_URL
import com.ssverma.showtime.data.remote.response.RemoteMovie
import com.ssverma.showtime.utils.*

class Movie(
    val id: Int,
    val tmdbId: String?,
    val title: String,
    val posterImageUrl: String,
    val tagline: String?,
    val backdropImageUrl: String?,
    val budget: Long,
    val status: String?,
    val videoAvailable: Boolean,
    val voteAvgPercentage: Float,
    val voteCount: Int,
    val displayReleaseDate: String?,
    val revenue: Long,
    val runtime: Int,
    val popularity: Float,
    val displayPopularity: String
)

fun RemoteMovie.asMovie(): Movie {
    return Movie(
        id = id,
        tmdbId = tmdbId,
        title = title ?: "",
        tagline = tagline,
        posterImageUrl = CoreUtils.buildImageUrl(TMDB_IMAGE_BASE_URL, posterPath),
        backdropImageUrl = CoreUtils.buildImageUrl(TMDB_IMAGE_BASE_URL, backdropPath),
        budget = budget,
        status = status,
        videoAvailable = videoAvailable,
        voteAvgPercentage = voteAvg * 10f,
        voteCount = voteCount,
        displayReleaseDate = DateUtils.parseIsoDate(releaseDate)?.formatLocally(),
        revenue = revenue,
        runtime = runtime,
        popularity = popularity,
        displayPopularity = FormatterUtils.toRangeSymbol(popularity)
    )
}

fun List<RemoteMovie>.asMovies(): List<Movie> {
    return map { it.asMovie() }
}