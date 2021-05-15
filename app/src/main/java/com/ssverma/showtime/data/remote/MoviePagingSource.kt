package com.ssverma.showtime.data.remote

import com.ssverma.showtime.api.makeTmdbApiRequest
import com.ssverma.showtime.data.remote.response.PagedPayload
import com.ssverma.showtime.data.remote.response.RemoteMovie
import com.ssverma.showtime.domain.ApiData
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.domain.model.asMovies
import retrofit2.Response
import javax.inject.Inject

class MoviePagingSource @Inject constructor(
    private val movieApiCall: suspend (page: Int) -> Response<PagedPayload<RemoteMovie>>
) : IndexPagingSource<Movie>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val currentPageNumber = params.key ?: 1

        val result: Result<ApiData<PagedPayload<RemoteMovie>>> = makeTmdbApiRequest(
            apiCall = { movieApiCall(currentPageNumber) }
        )

        return when (result) {
            is Result.Success -> {
                val payload = result.data.payload

                val nextPageNumber = currentPageNumber + 1

                val nextPageKey =
                    if (nextPageNumber > payload.pageCount || payload.results.isNullOrEmpty()) {
                        null
                    } else {
                        nextPageNumber
                    }

                LoadResult.Page(
                    data = payload.results?.asMovies() ?: emptyList(),
                    prevKey = null,
                    nextKey = nextPageKey
                )
            }
            is Result.Loading -> throw IllegalStateException("Loading state should not be emitted in one shot api request")
            is Result.Error -> {
                LoadResult.Error(result.cause)
            }
        }
    }
}