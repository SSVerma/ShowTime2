package com.ssverma.feature.auth.domain.repository

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure

interface AuthRepository {
    suspend fun fetchRequestToken(): Result<String, Failure.CoreFailure>

    suspend fun fetchAccessToken(): Result<String, Failure.CoreFailure>

    suspend fun isAuthorized(): Boolean

    suspend fun fetchSessionId(): Result<String, Failure.CoreFailure>

    suspend fun logout(): Result<Unit, Failure.CoreFailure>
}