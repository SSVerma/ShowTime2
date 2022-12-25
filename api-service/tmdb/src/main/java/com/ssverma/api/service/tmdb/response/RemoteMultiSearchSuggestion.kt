package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RemoteMultiSearchSuggestion(
    @SerializedName("media_type")
    val mediaType: String?,

    @SerializedName("id")
    val id: Int,

    @SerializedName("name", alternate = ["title"])
    val name: String?,

    @SerializedName("popularity")
    val popularity: Float,

    /*Person fields*/

    @SerializedName("profile_path")
    val profilePath: String?,

    @SerializedName("known_for_department")
    val department: String?,

    @SerializedName("gender")
    val gender: Int,

    /*Media fields*/

    @SerializedName("backdrop_path")
    val backdropPath: String?,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("overview")
    val overview: String?,

    @SerializedName("video")
    val videoAvailable: Boolean,

    @SerializedName("vote_average")
    val voteAvg: Float,

    @SerializedName("vote_count")
    val voteCount: Int,

    @SerializedName("original_language")
    val originalLanguage: String?,

    /*Movie fields*/

    @SerializedName("release_date")
    val releaseDate: String?,

    /*Tv fields*/

    @SerializedName("first_air_date")
    val firstAirDate: String?
)