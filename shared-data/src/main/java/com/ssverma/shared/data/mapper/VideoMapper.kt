package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.api.service.tmdb.response.RemoteVideo
import com.ssverma.shared.domain.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun RemoteVideo.asVideo(): Video {
    return Video(
        id = id.orEmpty(),
        key = key.orEmpty(),
        name = name.orEmpty(),
        site = site.orEmpty(),
        type = type.orEmpty(),
        official = official ?: false,
        publishedAt = publishedAt,
        iso6391 = iso6391
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
