package com.ssverma.showtime.domain.mapper

import com.ssverma.showtime.domain.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

abstract class Mapper<in I, out O> {
    abstract suspend fun map(input: I): O
}

abstract class ListMapper<in I, out O>(
    private val coroutineDispatcher: CoroutineDispatcher
) : Mapper<List<I>, List<O>>() {
    override suspend fun map(input: List<I>): List<O> {
        return withContext(coroutineDispatcher) {
            input.map { mapItem(it) }
        }
    }

    abstract suspend fun mapItem(input: I): O
}


abstract class ResultMapper<I, O>(
    private val coroutineDispatcher: CoroutineDispatcher
) : Mapper<Result<I>, Result<O>>() {
    override suspend fun map(input: Result<I>): Result<O> {
        return withContext(coroutineDispatcher) {
            when (input) {
                is Result.Success -> {
                    Result.Success(
                        data = this@ResultMapper.mapValue(input.data),
                        origin = input.origin
                    )
                }
                is Result.Loading -> {
                    Result.Loading(cachedData = input.cachedData?.let { mapValue(it) })
                }
                is Result.Error -> {
                    Result.Error(
                        displayMessage = input.displayMessage,
                        cause = input.cause,
                        payload = input.payload,
                        retry = input.retry
                    )
                }
            }
        }
    }

    abstract suspend fun mapValue(input: I): O
}