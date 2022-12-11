package com.ssverma.feature.account.domain.repository

import com.ssverma.feature.account.domain.model.MediaStats
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType

interface AccountRepository {
    suspend fun fetchProfile(
        sessionId: String
    ): Result<Profile, Failure.CoreFailure>

    suspend fun removeUserAccount()

    suspend fun fetchAccountStats(
        sessionId: String,
        mediaType: MediaType,
        mediaId: Int
    ): CoreResult<MediaStats>

    suspend fun toggleMediaFavoriteStatus(
        sessionId: String,
        mediaType: MediaType,
        mediaId: Int,
        favorite: Boolean
    ): CoreResult<Unit>

    suspend fun toggleMediaWatchlistStatus(
        sessionId: String,
        mediaType: MediaType,
        mediaId: Int,
        inWatchlist: Boolean
    ): CoreResult<Unit>
}