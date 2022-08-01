package com.ssverma.feature.auth.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.request.AccessTokenBody
import com.ssverma.api.service.tmdb.request.LogoutBody
import com.ssverma.api.service.tmdb.request.RequestTokenBody
import com.ssverma.api.service.tmdb.request.SessionBody
import com.ssverma.api.service.tmdb.response.AccessTokenPayload
import com.ssverma.api.service.tmdb.response.RequestTokenPayload
import com.ssverma.api.service.tmdb.response.SessionPayload

interface AuthRemoteDataSource {
    suspend fun createRequestToken(
        requestTokenBody: RequestTokenBody
    ): TmdbApiResponse<RequestTokenPayload>

    suspend fun createAccessToken(
        accessTokenBody: AccessTokenBody
    ): TmdbApiResponse<AccessTokenPayload>

    suspend fun createSessionId(
        sessionBody: SessionBody
    ): TmdbApiResponse<SessionPayload>

    suspend fun logout(
        logoutBody: LogoutBody
    ): TmdbApiResponse<Unit>
}