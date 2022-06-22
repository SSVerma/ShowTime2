package com.ssverma.feature.search.domain.model

import com.ssverma.shared.domain.model.Gender

sealed interface SearchSuggestion {
    data class Movie(
        val id: Int,
        val title: String,
        val overview: String,
        val posterImageUrl: String,
        val backdropImageUrl: String,
        val voteAvg: Float,
        val voteAvgPercentage: Float,
        val voteCount: Int,
        val displayReleaseDate: String?,
        val popularity: Float,
        val displayPopularity: String,
        val originalLanguage: String
    ) : SearchSuggestion

    data class TvShow(
        val id: Int,
        val title: String,
        val overview: String,
        val posterImageUrl: String,
        val backdropImageUrl: String,
        val voteAvg: Float,
        val voteAvgPercentage: Float,
        val voteCount: Int,
        val displayFirstAirDate: String?,
        val popularity: Float,
        val displayPopularity: String,
        val originalLanguage: String
    ) : SearchSuggestion

    data class Person(
        val id: Int,
        val name: String,
        val imageUrl: String,
        val department: String,
        val popularity: Float,
        val gender: Gender
    ) : SearchSuggestion

    object None : SearchSuggestion
}