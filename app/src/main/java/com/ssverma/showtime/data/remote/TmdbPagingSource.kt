package com.ssverma.showtime.data.remote

import com.ssverma.showtime.api.makeTmdbApiRequest
import com.ssverma.showtime.data.remote.response.PagedPayload
import com.ssverma.showtime.domain.ApiData
import com.ssverma.showtime.domain.Result
import retrofit2.Response
import javax.inject.Inject

open class TmdbPagingSource<R, D : Any> @Inject constructor(
    private val tmdbApiCall: suspend (page: Int) -> Response<PagedPayload<R>>,
    private val mapRemoteToDomain: suspend (remote: List<R>) -> List<D>
) : IndexPagingSource<D>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, D> {
        val currentPageNumber = params.key ?: 1

        val result: Result<ApiData<PagedPayload<R>>> = makeTmdbApiRequest(
            apiCall = { tmdbApiCall(currentPageNumber) }
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
                    data = payload.results?.let { mapRemoteToDomain(it) } ?: emptyList(),
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