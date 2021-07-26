package com.ssverma.showtime.data.remote

import com.ssverma.showtime.data.remote.response.PagedPayload
import com.ssverma.showtime.data.remote.response.RemoteReview
import com.ssverma.showtime.domain.model.Review
import com.ssverma.showtime.domain.model.asReviews
import retrofit2.Response

class ReviewsPagingSource(
    private val reviewsApiCall: suspend (page: Int) -> Response<PagedPayload<RemoteReview>>
) : TmdbPagingSource<RemoteReview, Review>(
    tmdbApiCall = { page -> reviewsApiCall(page) },
    mapRemoteToDomain = { it.asReviews() }
)