package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.RemoteImageShot
import com.ssverma.showtime.domain.model.ImageShot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImageShotMapper @Inject constructor() : Mapper<RemoteImageShot, ImageShot>() {
    override suspend fun map(input: RemoteImageShot): ImageShot {
        return input.asImageShot()
    }
}

class ImageShotsMapper @Inject constructor() : ListMapper<RemoteImageShot, ImageShot>() {
    override suspend fun mapItem(input: RemoteImageShot): ImageShot {
        return input.asImageShot()
    }
}

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