package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class VideoPayload(
    @SerializedName("results")
    val videos: List<RemoteVideo>?
)

class RemoteVideo(
    @SerializedName("id")
    val id: String?,

    @SerializedName("key")
    val key: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("iso_639_1")
    val iso6391: String?,

    @SerializedName("site")
    val site: String?,

    @SerializedName("size")
    val size: Int?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("official")
    val official: Boolean?,

    @SerializedName("published_at")
    val publishedAt: String?
)
