package com.ssverma.shared.domain.model.match

import kotlinx.serialization.Serializable

@Serializable
data class MatchProviderBadge(
    val providerId: Int,
    val providerName: String,
    val logoPath: String
)

@Serializable
data class MovieMatchCard(
    val id: Int,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val releaseYear: String? = null,
    val voteAvg: Float = 0f,
    val overview: String = "",
    val genreNames: List<String> = emptyList(),
    val watchProviders: List<MatchProviderBadge> = emptyList(),
    val runtime: Int? = null
)
