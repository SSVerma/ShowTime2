package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RemoteCredit(
    @SerializedName("cast")
    val casts: List<RemoteCast>?,

    @SerializedName("crew")
    val crews: List<RemoteCrew>?,

    @SerializedName("guest_stars")
    val guestStars: List<RemoteCast>?
)

class RemoteCast(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("gender")
    val gender: Int,

    @SerializedName("character")
    val character: String?,

    @SerializedName("profile_path")
    val profilePath: String?,

    @SerializedName("credit_id")
    val creditId: String?
)

class RemoteCrew(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("profile_path")
    val profilePath: String?,

    @SerializedName("credit_id")
    val creditId: String?,

    @SerializedName("department")
    val department: String?,

    @SerializedName("job")
    val job: String?,
)