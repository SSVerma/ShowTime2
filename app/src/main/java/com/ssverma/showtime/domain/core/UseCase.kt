package com.ssverma.showtime.domain.core

import com.ssverma.showtime.domain.DomainResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

abstract class UseCase<in P, S, E>(private val coroutineDispatcher: CoroutineDispatcher) {
    suspend operator fun invoke(params: P): DomainResult<S, E> {
        return withContext(coroutineDispatcher) {
            execute(params = params)
        }
    }

    protected abstract suspend fun execute(params: P): DomainResult<S, E>
}

abstract class FlowUseCase<in P, S, E>(private val coroutineDispatcher: CoroutineDispatcher) {

    operator fun invoke(params: P): Flow<DomainResult<S, E>> {
        return execute(params = params)
            .flowOn(coroutineDispatcher)
    }

    protected abstract fun execute(params: P): Flow<DomainResult<S, E>>
}