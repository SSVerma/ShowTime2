package com.ssverma.shared.domain.model.movie

import com.ssverma.shared.domain.utils.DateUtils

data class MoviePreview(
    val id: Int,
    val title: String,
    val overview: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val voteAvg: Float,
    val voteAvgPercentage: Float,
    val voteCount: Int,
    val displayReleaseDate: String?,
    val displayYear: String,
    val popularity: Float,
    val displayPopularity: String,
    val genreIds: List<Int>,
    val adult: Boolean
)

fun Movie.asMoviePreview(): MoviePreview {
    return MoviePreview(
        id = id,
        title = title,
        overview = overview,
        posterImageUrl = posterImageUrl,
        backdropImageUrl = backdropImageUrl,
        voteAvg = voteAvg,
        voteAvgPercentage = voteAvgPercentage,
        voteCount = voteCount,
        displayReleaseDate = displayReleaseDate,
        displayYear = DateUtils.parseIsoDate(displayReleaseDate)?.year?.toString().orEmpty(),
        popularity = popularity,
        displayPopularity = displayPopularity,
        genreIds = generes.map { it.id },
        adult = false,
    )
}
