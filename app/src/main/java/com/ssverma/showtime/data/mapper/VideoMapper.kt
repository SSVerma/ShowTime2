package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.api.service.tmdb.response.RemoteVideo
import com.ssverma.core.domain.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun RemoteVideo.asVideo(): Video {
    return Video(
        id = id.orEmpty(),
        key = key.orEmpty(),
        iso6391 = iso6391,
        site = site.orEmpty(),
        type = type.orEmpty()
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