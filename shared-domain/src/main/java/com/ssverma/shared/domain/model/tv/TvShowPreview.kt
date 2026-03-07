package com.ssverma.shared.domain.model.tv

data class TvShowPreview(
    val id: Int,
    val title: String,
    val overview: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val voteAvg: Float,
    val voteAvgPercentage: Float,
    val voteCount: Int,
    val displayFirstAirDate: String?,
    val displayYear: String,
    val popularity: Float,
    val displayPopularity: String,
    val genreIds: List<Int>,
)

fun TvShow.asTvShowPreview(): TvShowPreview {
    return TvShowPreview(
        id = id,
        title = title,
        overview = overview,
        posterImageUrl = posterImageUrl,
        backdropImageUrl = backdropImageUrl,
        voteAvg = voteAvg,
        voteAvgPercentage = voteAvgPercentage,
        voteCount = voteCount,
        displayFirstAirDate = displayFirstAirDate,
        displayYear = displayFirstAirDate?.split(" ")?.lastOrNull() ?: "",
        popularity = popularity,
        displayPopularity = displayPopularity,
        genreIds = generes.map { it.id },
    )
}
