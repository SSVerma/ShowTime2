package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RemoteCollectionDetails(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("overview")
    val overview: String?,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("backdrop_path")
    val backdropPath: String?,

    @SerializedName("parts")
    val parts: List<RemoteMovie>?
)
