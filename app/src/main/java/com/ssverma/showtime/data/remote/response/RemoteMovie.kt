package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

class RemoteMovie(
    @SerializedName("id")
    val id: Int,

    @SerializedName("tmdb_id")
    val tmdbId: String?,

    @SerializedName("backdrop_path")
    val backdropPath: String?,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("budget")
    val budget: Long,

    @SerializedName("title")
    val title: String?,

    @SerializedName("tagline")
    val tagline: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("video")
    val videoAvailable: Boolean,

    @SerializedName("vote_average")
    val voteAvg: Float,

    @SerializedName("vote_count")
    val voteCount: Int,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("revenue")
    val revenue: Long,

    @SerializedName("runtime")
    val runtime: Int,

    @SerializedName("popularity")
    val popularity: Float
)