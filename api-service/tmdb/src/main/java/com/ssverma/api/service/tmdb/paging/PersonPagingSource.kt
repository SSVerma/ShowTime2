package com.ssverma.api.service.tmdb.paging

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemotePerson

class PersonPagingSource<D : Any>(
    private val personApiCall: suspend (page: Int) -> TmdbApiResponse<PagedPayload<RemotePerson>>,
    private val mapRemoteToDomain: suspend (remote: List<RemotePerson>) -> List<D>
) : TmdbPagingSource<RemotePerson, D>(
    tmdbApiCall = { page -> personApiCall(page) },
    mapRemoteToDomain = { mapRemoteToDomain(it) }
)