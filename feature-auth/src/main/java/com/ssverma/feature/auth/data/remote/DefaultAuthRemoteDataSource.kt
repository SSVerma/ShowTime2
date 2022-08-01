package com.ssverma.feature.auth.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.request.AccessTokenBody
import com.ssverma.api.service.tmdb.request.LogoutBody
import com.ssverma.api.service.tmdb.request.RequestTokenBody
import com.ssverma.api.service.tmdb.request.SessionBody
import com.ssverma.api.service.tmdb.response.AccessTokenPayload
import com.ssverma.api.service.tmdb.response.RequestTokenPayload
import com.ssverma.api.service.tmdb.response.SessionPayload
import javax.inject.Inject

class DefaultAuthRemoteDataSource @Inject constructor(
    private val tmdbApiService: TmdbApiService
) : AuthRemoteDataSource {

    override suspend fun createRequestToken(
        requestTokenBody: RequestTokenBody
    ): TmdbApiResponse<RequestTokenPayload> {
        return tmdbApiService.createRequestToken(requestTokenBody = requestTokenBody)
    }

    override suspend fun createAccessToken(
        accessTokenBody: AccessTokenBody
    ): TmdbApiResponse<AccessTokenPayload> {
        return tmdbApiService.createAccessToken(accessTokenBody = accessTokenBody)
    }

    override suspend fun createSessionId(
        sessionBody: SessionBody
    ): TmdbApiResponse<SessionPayload> {
        return tmdbApiService.createSessionId(
            sessionBody = sessionBody
        )
    }

    override suspend fun logout(logoutBody: LogoutBody): TmdbApiResponse<Unit> {
        return tmdbApiService.logout(logoutBody = logoutBody)
    }
}