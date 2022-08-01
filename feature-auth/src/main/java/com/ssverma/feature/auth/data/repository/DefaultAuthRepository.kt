package com.ssverma.feature.auth.data.repository

import com.ssverma.api.service.tmdb.request.AccessTokenBody
import com.ssverma.api.service.tmdb.request.LogoutBody
import com.ssverma.api.service.tmdb.request.RequestTokenBody
import com.ssverma.api.service.tmdb.request.SessionBody
import com.ssverma.core.di.AppScoped
import com.ssverma.feature.auth.data.local.AuthLocalDataSource
import com.ssverma.feature.auth.data.remote.AuthRemoteDataSource
import com.ssverma.feature.auth.domain.defaults.AuthDefaults
import com.ssverma.feature.auth.domain.repository.AuthRepository
import com.ssverma.shared.data.mapper.asDomainResult
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class DefaultAuthRepository @Inject constructor(
    @AppScoped
    private val externalScope: CoroutineScope,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authLocalDataSource: AuthLocalDataSource
) : AuthRepository {

    override suspend fun fetchRequestToken(): Result<String, Failure.CoreFailure> {
        val requestTokenApiResult = authRemoteDataSource.createRequestToken(
            requestTokenBody = RequestTokenBody(
                redirectTo = AuthDefaults.AuthApprovedRedirectDeepLink
            )
        )

        return requestTokenApiResult.asDomainResult { response ->
            externalScope.launch {
                authLocalDataSource.persistRequestToken(
                    requestToken = response.body.requestToken.orEmpty()
                )
            }.join()

            response.body.requestToken.orEmpty()
        }
    }

    override suspend fun fetchAccessToken(): Result<String, Failure.CoreFailure> {
        val accessToken = authLocalDataSource.loadAccessToken()

        if (accessToken.isNotEmpty()) {
            return Result.Success(data = accessToken)
        }

        val lastRequestToken = authLocalDataSource.loadRequestToken()

        if (lastRequestToken.isEmpty()) {
            authLocalDataSource.clearAccessToken()
            return Result.Error(error = Failure.CoreFailure.UnexpectedFailure)
        }

        val accessTokenApiResult = authRemoteDataSource.createAccessToken(
            accessTokenBody = AccessTokenBody(requestToken = lastRequestToken)
        )

        return accessTokenApiResult.asDomainResult { response ->
            authLocalDataSource.clearRequestToken()

            response.body.accessToken.orEmpty().also { accessToken ->
                authLocalDataSource.persistAccessToken(
                    accessToken = accessToken
                )
            }
        }
    }

    override suspend fun isAuthorized(): Boolean {
        return authLocalDataSource.loadAccessToken().isNotEmpty()
    }

    override suspend fun fetchSessionId(): Result<String, Failure.CoreFailure> {
        val sessionId = authLocalDataSource.loadSessionId()

        if (sessionId.isNotEmpty()) {
            return Result.Success(data = sessionId)
        }

        val accessToken = authLocalDataSource.loadAccessToken()

        val apiResult = authRemoteDataSource.createSessionId(
            sessionBody = SessionBody(accessToken = accessToken)
        )

        return apiResult.asDomainResult {
            it.body.sessionId.orEmpty().also { sessionId ->
                authLocalDataSource.persistSessionId(sessionId = sessionId)
            }
        }
    }

    override suspend fun logout(): Result<Unit, Failure.CoreFailure> {
        val accessToken = authLocalDataSource.loadAccessToken()
        if (accessToken.isEmpty()) {
            return Result.Success(data = Unit)
        }

        val apiResult = authRemoteDataSource.logout(
            logoutBody = LogoutBody(
                accessToken = accessToken
            )
        )

        return apiResult.asDomainResult {
            externalScope.launch {
                authLocalDataSource.clearAccessToken()
                authLocalDataSource.clearSessionId()
            }.join()
        }
    }
}