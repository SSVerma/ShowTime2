package com.ssverma.showtime.domain.model

import com.ssverma.showtime.api.convertToFullTmdbImageUrl
import com.ssverma.showtime.data.remote.response.RemoteImageShot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImageShot(
    val aspectRatio: Float,
    val imageUrl: String,
    val height: Int,
    val width: Int,
    val iso6391: String?
)

fun RemoteImageShot.asImageShot(): ImageShot {
    return ImageShot(
        aspectRatio = aspectRatio,
        imageUrl = imagePath.convertToFullTmdbImageUrl(),
        height = height,
        width = width,
        iso6391 = iso6391 ?: ""
    )
}

suspend fun List<RemoteImageShot>.asImagesShots() = withContext(Dispatchers.Default) {
    map { it.asImageShot() }
}

fun emptyImageShot() = ImageShot(
    aspectRatio = 0f,
    imageUrl = "",
    height = 0,
    width = 0,
    iso6391 = null
)