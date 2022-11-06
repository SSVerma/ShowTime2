package com.ssverma.feature.account.data.repository

import com.ssverma.feature.account.data.mapper.ProfileMapper
import com.ssverma.feature.account.data.remote.AccountRemoteDataSource
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.shared.data.mapper.asDomainResult
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import javax.inject.Inject

internal class DefaultAccountRepository @Inject constructor(
    private val accountRemoteDataSource: AccountRemoteDataSource,
    private val profileMapper: ProfileMapper
) : AccountRepository {

    override suspend fun fetchProfile(sessionId: String): Result<Profile, Failure.CoreFailure> {
        return accountRemoteDataSource
            .fetchAccountDetails(sessionId = sessionId)
            .asDomainResult {
                profileMapper.map(it.body)
            }
    }
}