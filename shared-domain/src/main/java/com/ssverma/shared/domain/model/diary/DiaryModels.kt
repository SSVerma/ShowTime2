package com.ssverma.shared.domain.model.diary

import com.ssverma.shared.domain.model.MediaType

data class DiaryEntry(
    val id: Long = 0L,
    val mediaId: Int,
    val mediaType: MediaType,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String = "",
    val releaseDate: String = "",
    val tmdbRating: Float = 0f,
    val userRating: Float, // 0.5 to 5.0 (or 1 to 10)
    val review: String = "",
    val isRewatch: Boolean = false,
    val loggedAt: Long = System.currentTimeMillis(),
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null
)

data class DiarySummaryStats(
    val totalLogged: Int = 0,
    val totalMovies: Int = 0,
    val totalTvShows: Int = 0,
    val averageUserRating: Float = 0f,
    val rewatchCount: Int = 0,
    val fiveStarCount: Int = 0
)

enum class DiaryFilterType {
    ALL,
    MOVIES_ONLY,
    TV_ONLY,
    REWATCHES_ONLY,
    FIVE_STARS_ONLY
}
