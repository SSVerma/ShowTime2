package com.ssverma.feature.account.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.response.AccountPayload

interface AccountRemoteDataSource {
    suspend fun fetchAccountDetails(
        sessionId: String
    ): TmdbApiResponse<AccountPayload>
}