package com.ssverma.showtime.data.remote

import com.ssverma.showtime.data.remote.response.PagedPayload
import com.ssverma.showtime.data.remote.response.RemoteMovie
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.domain.model.asMovies
import retrofit2.Response

@Deprecated("use-> api.service.tmdb")
class MoviePagingSource(
    private val movieApiCall: suspend (page: Int) -> Response<PagedPayload<RemoteMovie>>
) : TmdbPagingSource<RemoteMovie, Movie>(
    tmdbApiCall = { page -> movieApiCall(page) },
    mapRemoteToDomain = { it.asMovies() }
)