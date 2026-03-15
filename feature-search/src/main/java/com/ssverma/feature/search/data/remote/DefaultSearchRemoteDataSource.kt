package com.ssverma.feature.search.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteCompany
import com.ssverma.api.service.tmdb.response.RemoteKeyword
import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion
import javax.inject.Inject

class DefaultSearchRemoteDataSource @Inject constructor(
    private val tmdbApiService: TmdbApiService
) : SearchRemoteDataSource {

    override suspend fun performMultiSearch(
        query: String
    ): TmdbApiResponse<PagedPayload<RemoteMultiSearchSuggestion>> {
        return tmdbApiService.multiSearch(query = query)
    }

    override suspend fun searchKeywords(query: String): TmdbApiResponse<PagedPayload<RemoteKeyword>> {
        return tmdbApiService.searchKeywords(query = query)
    }

    override suspend fun searchCompanies(query: String): TmdbApiResponse<PagedPayload<RemoteCompany>> {
        return tmdbApiService.searchCompanies(query = query)
    }
}
