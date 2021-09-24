package com.ssverma.showtime.data.remote

import com.ssverma.showtime.data.remote.response.PagedPayload
import com.ssverma.showtime.data.remote.response.RemoteTvShow
import com.ssverma.showtime.domain.model.TvShow
import com.ssverma.showtime.domain.model.asTvShows
import retrofit2.Response
import javax.inject.Inject

class TvShowsPagingSource @Inject constructor(
    private val tvShowApiCall: suspend (page: Int) -> Response<PagedPayload<RemoteTvShow>>
) : TmdbPagingSource<RemoteTvShow, TvShow>(
    tmdbApiCall = { tvShowApiCall(it) },
    mapRemoteToDomain = { it.asTvShows() }
)