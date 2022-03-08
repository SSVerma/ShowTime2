package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.response.RemoteImageShot
import com.ssverma.showtime.api.convertToFullTmdbImageUrl
import com.ssverma.showtime.domain.model.ImageShot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun RemoteImageShot.asImageShot(): ImageShot {
    return ImageShot(
        aspectRatio = aspectRatio,
        imageUrl = imagePath.convertToFullTmdbImageUrl(),
        height = height,
        width = width,
        iso6391 = iso6391.orEmpty()
    )
}

suspend fun List<RemoteImageShot>.asImagesShots() = withContext(Dispatchers.Default) {
    map { it.asImageShot() }
}