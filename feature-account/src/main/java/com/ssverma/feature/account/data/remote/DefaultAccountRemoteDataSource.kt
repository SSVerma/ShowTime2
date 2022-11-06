package com.ssverma.feature.account.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.AccountPayload
import javax.inject.Inject

class DefaultAccountRemoteDataSource @Inject constructor(
    private val tmdbApiService: TmdbApiService
) : AccountRemoteDataSource {

    override suspend fun fetchAccountDetails(sessionId: String): TmdbApiResponse<AccountPayload> {
        return tmdbApiService.getAccount(sessionId = sessionId)
    }
}