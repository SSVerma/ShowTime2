package com.ssverma.feature.search.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion

interface SearchRemoteDataSource {
    suspend fun performMultiSearch(
        query: String
    ): TmdbApiResponse<PagedPayload<RemoteMultiSearchSuggestion>>
}