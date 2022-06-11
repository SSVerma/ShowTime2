package com.ssverma.api.service.tmdb.paging

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteMovie

class MoviePagingSource<D : Any>(
    private val movieApiCall: suspend (page: Int) -> TmdbApiResponse<PagedPayload<RemoteMovie>>,
    private val mapRemoteToDomain: suspend (remote: List<RemoteMovie>) -> List<D>
) : TmdbPagingSource<RemoteMovie, D>(
    tmdbApiCall = { movieApiCall(it) },
    mapRemoteToDomain = { mapRemoteToDomain(it) }
)