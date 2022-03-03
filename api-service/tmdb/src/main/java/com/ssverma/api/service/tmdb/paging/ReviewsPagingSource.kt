package com.ssverma.api.service.tmdb.paging

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteReview

class ReviewsPagingSource<D : Any>(
    private val reviewsApiCall: suspend (page: Int) -> TmdbApiResponse<PagedPayload<RemoteReview>>,
    private val mapRemoteToDomain: suspend (remote: List<RemoteReview>) -> List<D>
) : TmdbPagingSource<RemoteReview, D>(
    tmdbApiCall = { page -> reviewsApiCall(page) },
    mapRemoteToDomain = { mapRemoteToDomain(it) }
)