package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

@Deprecated("use -> api.service.tmdb")
class ImagePayload(
    @SerializedName("posters")
    val posters: List<RemoteImageShot>?,

    @SerializedName("backdrops")
    val backdrops: List<RemoteImageShot>?,

    @SerializedName("stills")
    val stills: List<RemoteImageShot>?
)

@Deprecated("use -> api.service.tmdb")
class RemoteImageShot(
    @SerializedName("aspect_ratio")
    val aspectRatio: Float,

    @SerializedName("file_path")
    val imagePath: String?,

    @SerializedName("height")
    val height: Int,

    @SerializedName("width")
    val width: Int,

    @SerializedName("iso_639_1")
    val iso6391: String?
)