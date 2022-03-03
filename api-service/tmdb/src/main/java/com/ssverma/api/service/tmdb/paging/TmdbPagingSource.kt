package com.ssverma.api.service.tmdb.paging

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.TmdbErrorPayload
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.paging.IndexPagingSource
import com.ssverma.core.paging.PagingFailure

open class TmdbPagingSource<R, D : Any> constructor(
    private val tmdbApiCall: suspend (page: Int) -> TmdbApiResponse<PagedPayload<R>>,
    private val mapRemoteToDomain: suspend (remote: List<R>) -> List<D>
) : IndexPagingSource<D>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, D> {
        val currentPageNumber = params.key ?: 1

        val result: ApiResponse<PagedPayload<R>, TmdbErrorPayload> = tmdbApiCall(currentPageNumber)

        return when (result) {
            is ApiResponse.Success -> {
                val pagedPayload = result.body

                val nextPageNumber = currentPageNumber + 1

                val nextPageKey =
                    if (nextPageNumber > pagedPayload.pageCount || pagedPayload.results.isNullOrEmpty()) {
                        null
                    } else {
                        nextPageNumber
                    }

                LoadResult.Page(
                    data = pagedPayload.results?.let { mapRemoteToDomain(it) } ?: emptyList(),
                    prevKey = null,
                    nextKey = nextPageKey
                )
            }
            is ApiResponse.Error.ClientError -> {
                LoadResult.Error(PagingFailure.ClientFailure)
            }
            is ApiResponse.Error.NetworkError -> {
                LoadResult.Error(PagingFailure.NetworkFailure)
            }
            is ApiResponse.Error.ServerError -> {
                LoadResult.Error(PagingFailure.ServiceFailure)
            }
            is ApiResponse.Error.UnexpectedError -> {
                LoadResult.Error(PagingFailure.UnexpectedFailure)
            }
        }
    }
}