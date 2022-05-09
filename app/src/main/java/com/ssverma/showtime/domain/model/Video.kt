package com.ssverma.showtime.domain.model

private const val YoutubeVideoUrl = "https://www.youtube.com/watch?v="
private const val YoutubeThumbnailUrl = "https://img.youtube.com/vi/"

data class Video(
    val id: String,
    val key: String,
    val iso6391: String?,
    val site: String,
    val type: String
)

fun Video.youtubeThumbnailUrl(): String {
    return "$YoutubeThumbnailUrl$key/mqdefault.jpg"
}