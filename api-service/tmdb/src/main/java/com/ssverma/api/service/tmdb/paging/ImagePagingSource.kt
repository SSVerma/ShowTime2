package com.ssverma.api.service.tmdb.paging

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteImageShot

class ImagePagingSource<D : Any>(
    private val imagesApiCall: suspend (page: Int) -> TmdbApiResponse<PagedPayload<RemoteImageShot>>,
    private val mapRemoteToDomain: suspend (remote: List<RemoteImageShot>) -> List<D>
) : TmdbPagingSource<RemoteImageShot, D>(
    tmdbApiCall = { page -> imagesApiCall(page) },
    mapRemoteToDomain = { mapRemoteToDomain(it) }
)