package com.ssverma.feature.account.domain.repository

import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure

interface AccountRepository {
    suspend fun fetchProfile(
        sessionId: String
    ): Result<Profile, Failure.CoreFailure>

    suspend fun removeUserAccount()
}