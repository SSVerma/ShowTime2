package com.ssverma.shared.domain.model.match

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class MatchProviderBadge(
    @SerializedName("providerId")
    val providerId: Int,
    @SerializedName("providerName")
    val providerName: String,
    @SerializedName("logoPath")
    val logoPath: String
)

@Serializable
data class MovieMatchCard(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("posterImageUrl")
    val posterImageUrl: String,
    @SerializedName("backdropImageUrl")
    val backdropImageUrl: String,
    @SerializedName("releaseYear")
    val releaseYear: String? = null,
    @SerializedName("voteAvg")
    val voteAvg: Float = 0f,
    @SerializedName("overview")
    val overview: String = "",
    @SerializedName("genreNames")
    val genreNames: List<String> = emptyList(),
    @SerializedName("watchProviders")
    val watchProviders: List<MatchProviderBadge> = emptyList(),
    @SerializedName("runtime")
    val runtime: Int? = null
)
