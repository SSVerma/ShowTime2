package com.ssverma.shared.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

abstract class UseCase<in P, T>(private val coroutineDispatcher: CoroutineDispatcher) {
    suspend operator fun invoke(params: P): T {
        return withContext(coroutineDispatcher) {
            execute(params = params)
        }
    }

    protected abstract suspend fun execute(params: P): T
}

abstract class NoParamUseCase<T>(private val coroutineDispatcher: CoroutineDispatcher) {
    suspend operator fun invoke(): T {
        return withContext(coroutineDispatcher) {
            execute()
        }
    }

    protected abstract suspend fun execute(): T
}

abstract class FlowUseCase<in P, T>(private val coroutineDispatcher: CoroutineDispatcher) {
    operator fun invoke(params: P): Flow<T> {
        return execute(params = params)
            .flowOn(coroutineDispatcher)
    }

    protected abstract fun execute(params: P): Flow<T>
}

abstract class NoParamFlowUseCase<T>(private val coroutineDispatcher: CoroutineDispatcher) {
    operator fun invoke(): Flow<T> {
        return execute().flowOn(coroutineDispatcher)
    }

    protected abstract fun execute(): Flow<T>
}