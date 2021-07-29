package com.ssverma.showtime.domain.model

import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.remote.response.RemoteVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val YoutubeVideoUrl = "https://www.youtube.com/watch?v="
private const val YoutubeThumbnailUrl = "https://img.youtube.com/vi/"

data class Video(
    val id: String,
    val key: String,
    val iso6391: String?,
    val site: String,
    val type: String
)

fun RemoteVideo.asVideo(): Video {
    return Video(
        id = id ?: "",
        key = key ?: "",
        iso6391 = iso6391,
        site = site ?: "",
        type = type ?: ""
    )
}

suspend fun List<RemoteVideo>.asVideos() = withContext(Dispatchers.Default) {
    map { it.asVideo() }
}

suspend fun List<RemoteVideo>.filterYoutubeVideos() = withContext(Dispatchers.Default) {
    filter {
        TmdbApiTiedConstants.AvailableVideoSites.Youtube.equals(
            it.site,
            ignoreCase = true
        )
    }.asVideos()
}

fun Video.youtubeVideoUrl(): String {
    return YoutubeVideoUrl + key
}

fun Video.youtubeThumbnailUrl(): String {
    return "$YoutubeThumbnailUrl$key/mqdefault.jpg"
}