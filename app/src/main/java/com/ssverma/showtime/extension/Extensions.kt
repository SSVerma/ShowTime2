package com.ssverma.showtime.extension

import com.ssverma.showtime.domain.ApiData
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.mapper.RemoteToDomainMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <R, D> Flow<Result<ApiData<R>>>.asDomainFlow(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
    mapApiData: suspend (input: ApiData.Success<R>) -> D
): Flow<Result<D>> {
    val mapper = object : RemoteToDomainMapper<R, D>(coroutineDispatcher) {
        override suspend fun mapValue(input: ApiData.Success<R>): D {
            return mapApiData(input)
        }
    }

    return map { mapper.map(it) }
}