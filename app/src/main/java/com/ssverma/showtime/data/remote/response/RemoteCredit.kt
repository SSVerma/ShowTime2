package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

@Deprecated("use-> api.service.tmdb")
class RemoteCredit(
    @SerializedName("cast")
    val casts: List<RemoteCast>?,

    @SerializedName("crew")
    val crews: List<RemoteCrew>?,

    @SerializedName("guest_stars")
    val guestStars: List<RemoteCast>?
)

@Deprecated("use-> api.service.tmdb")
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

@Deprecated("use-> api.service.tmdb")
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