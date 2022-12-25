package com.ssverma.shared.data.mapper

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class Mapper<in I, out O> {
    abstract suspend fun map(input: I): O
}

abstract class ListMapper<in I, out O>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : Mapper<List<I>, List<O>>() {
    override suspend fun map(input: List<I>): List<O> {
        return withContext(coroutineDispatcher) {
            input.map { mapItem(it) }
        }
    }

    abstract suspend fun mapItem(input: I): O
}

abstract class MultiMapper<in Remote, in Local, out Domain> {
    abstract suspend fun mapRemote(remote: Remote): Domain

    abstract suspend fun mapLocal(local: Local): Domain
}