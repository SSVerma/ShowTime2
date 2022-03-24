package com.ssverma.showtime.domain.model.movie

import com.ssverma.showtime.R
import com.ssverma.showtime.api.TMDB_IMAGE_BASE_URL
import com.ssverma.showtime.data.remote.response.RemoteMovie
import com.ssverma.showtime.domain.model.*
import com.ssverma.showtime.extension.emptyIfAbsent
import com.ssverma.showtime.extension.emptyIfNull
import com.ssverma.showtime.utils.CoreUtils
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.FormatterUtils
import com.ssverma.showtime.utils.formatLocally
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Movie(
    val id: Int,
    val imdbId: String?,
    val title: String,
    val tagline: String?,
    val overview: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val budget: Long,
    val status: String,
    val videoAvailable: Boolean,
    val voteAvg: Float,
    val voteAvgPercentage: Float,
    val voteCount: Int,
    val displayReleaseDate: String?,
    val revenue: Long,
    val runtime: Int,
    val popularity: Float,
    val displayPopularity: String,
    val originalLanguage: String,
    val movieCollection: MovieCollection?,
    val casts: List<Cast>,
    val crews: List<Crew>,
    val keywords: List<Keyword>,
    val posters: List<ImageShot>,
    val backdrops: List<ImageShot>,
    val videos: List<Video>,
    val generes: List<Genre>,
    val reviews: List<Review>,
    val similarMovies: List<Movie>,
    val recommendations: List<Movie>,
)

suspend fun RemoteMovie.asMovie(): Movie {
    return Movie(
        id = id,
        imdbId = imdbId,
        title = title ?: "",
        tagline = if (tagline.isNullOrEmpty()) null else tagline,
        overview = overview ?: "",
        posterImageUrl = CoreUtils.buildImageUrl(TMDB_IMAGE_BASE_URL, posterPath),
        backdropImageUrl = CoreUtils.buildImageUrl(TMDB_IMAGE_BASE_URL, backdropPath),
        budget = budget,
        status = status ?: "",
        videoAvailable = videoAvailable,
        voteAvg = voteAvg,
        voteAvgPercentage = voteAvg * 10f,
        voteCount = voteCount,
        displayReleaseDate = DateUtils.parseIsoDate(releaseDate)?.formatLocally(),
        revenue = revenue,
        runtime = runtime,
        popularity = popularity,
        displayPopularity = FormatterUtils.toRangeSymbol(popularity),
        originalLanguage = originalLanguage ?: "",
        casts = credit?.casts?.asCasts() ?: emptyList(),
        crews = credit?.crews?.asCrews() ?: emptyList(),
        keywords = keywordPayload?.keywords?.asKeywords() ?: emptyList(),
        posters = imagePayload?.posters?.asImagesShots() ?: emptyList(),
        backdrops = imagePayload?.backdrops?.asImagesShots() ?: emptyList(),
        videos = videoPayload?.videos?.filterYoutubeVideos() ?: emptyList(),
        generes = genres?.asGenres() ?: emptyList(),
        movieCollection = collection?.asMovieCollection(),
        reviews = reviews?.results?.asReviews()?.asReversed() ?: emptyList(),
        similarMovies = similarMovies?.results?.asMovies() ?: emptyList(),
        recommendations = recommendations?.results?.asMovies() ?: emptyList(),
    )
}

suspend fun List<RemoteMovie>.asMovies(): List<Movie> {
    return withContext(Dispatchers.Default) {
        map { it.asMovie() }
    }
}

fun Movie.imageShots(): List<ImageShot> {
    return (backdrops + posters)
}

fun Movie.topImageShots(
    max: Int = 9,
    from: List<ImageShot> = imageShots(),
): List<ImageShot> {
    return if (from.size <= max) {
        from
    } else {
        from.subList(0, max)
    }
}

fun Movie.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent()
        ),
        Highlight(
            labelRes = R.string.release_date,
            value = displayReleaseDate.emptyIfNull(),
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
            labelRes = R.string.runtime,
            value = if (runtime == 0) runtime.emptyIfAbsent() else DateUtils.formatMinutes(runtime)
        ),
        Highlight(
            labelRes = R.string.revenue,
            value = if (revenue == 0L) revenue.emptyIfAbsent() else "$$revenue"
        )
    )
}