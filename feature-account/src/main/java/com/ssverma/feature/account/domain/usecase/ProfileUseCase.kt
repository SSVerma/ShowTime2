package com.ssverma.feature.account.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class ProfileUseCase @Inject constructor(
    @DefaultDispatcher
    dispatcher: CoroutineDispatcher,
    private val accountRepository: AccountRepository
) : UseCase<String, Result<Profile, Failure.CoreFailure>>(dispatcher) {

    override suspend fun execute(params: String): Result<Profile, Failure.CoreFailure> {
        return accountRepository.fetchProfile(sessionId = params)
    }
}