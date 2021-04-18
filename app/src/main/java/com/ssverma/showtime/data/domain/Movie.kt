package com.ssverma.showtime.data.domain

import com.ssverma.showtime.api.TMDB_IMAGE_BASE_URL
import com.ssverma.showtime.data.remote.response.RemoteMovie
import com.ssverma.showtime.utils.CoreUtils

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
    val popularity: Float
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
        displayReleaseDate = releaseDate,
        revenue = revenue,
        runtime = runtime,
        popularity = popularity
    )
}

/*suspend*/ fun List<RemoteMovie>.asMovies(): List<Movie> {
//    return withContext(Dispatchers.Default) {
//        this@asMovies.map { it.asMovie() }
//    }
    return map { it.asMovie() }
}