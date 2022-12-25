package com.ssverma.feature.auth.data.local

interface AuthLocalDataSource {
    suspend fun persistAccessToken(accessToken: String)

    suspend fun loadAccessToken(): String

    suspend fun clearAccessToken()

    suspend fun persistRequestToken(requestToken: String)

    suspend fun loadRequestToken(): String

    suspend fun clearRequestToken()

    suspend fun persistSessionId(sessionId: String)

    suspend fun loadSessionId(): String

    suspend fun clearSessionId()
}