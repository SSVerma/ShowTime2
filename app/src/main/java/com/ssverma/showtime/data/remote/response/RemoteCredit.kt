package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

class RemoteCredit(
    @SerializedName("cast")
    val casts: List<RemoteCast>?,

    @SerializedName("crew")
    val crews: List<RemoteCrew>?
)

class RemoteCast(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?,

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