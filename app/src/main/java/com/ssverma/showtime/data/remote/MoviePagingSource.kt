package com.ssverma.showtime.data.remote

import com.ssverma.showtime.data.remote.response.PagedPayload
import com.ssverma.showtime.data.remote.response.RemoteImageShot
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.domain.model.asImagesShots
import retrofit2.Response

class ImagePagingSource(
    private val imagesApiCall: suspend (page: Int) -> Response<PagedPayload<RemoteImageShot>>
) : TmdbPagingSource<RemoteImageShot, ImageShot>(
    tmdbApiCall = { page -> imagesApiCall(page) },
    mapRemoteToDomain = { it.asImagesShots() }
)