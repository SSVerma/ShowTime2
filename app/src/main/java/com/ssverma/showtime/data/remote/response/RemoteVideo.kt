package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

@Deprecated("use -> api.service.tmdb")
class VideoPayload(
    @SerializedName("results")
    val videos: List<RemoteVideo>?
)

@Deprecated("use -> api.service.tmdb")
class RemoteVideo(
    @SerializedName("id")
    val id: String?,

    @SerializedName("key")
    val key: String?,

    @SerializedName("iso_639_1")
    val iso6391: String?,

    @SerializedName("site")
    val site: String?,

    @SerializedName("type")
    val type: String?
)