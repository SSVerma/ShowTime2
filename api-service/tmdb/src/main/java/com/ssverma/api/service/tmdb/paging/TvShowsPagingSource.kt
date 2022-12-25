package com.ssverma.api.service.tmdb.paging

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteTvShow

class TvShowsPagingSource<D : Any>(
    private val tvShowApiCall: suspend (page: Int) -> TmdbApiResponse<PagedPayload<RemoteTvShow>>,
    private val mapRemoteToDomain: suspend (remote: List<RemoteTvShow>) -> List<D>
) : TmdbPagingSource<RemoteTvShow, D>(
    tmdbApiCall = { tvShowApiCall(it) },
    mapRemoteToDomain = { mapRemoteToDomain(it) }
)