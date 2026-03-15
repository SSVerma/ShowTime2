package com.ssverma.shared.domain

import com.ssverma.shared.domain.failure.Failure

sealed class Result<out S, out E> {
    data class Success<S>(
        val data: S
    ) : Result<S, Nothing>()

    data class Error<E>(
        val error: E
    ) : Result<Nothing, E>()

    inline fun onSuccess(action: (S) -> Unit): Result<S, E> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    inline fun onFailure(action: (E) -> Unit): Result<S, E> {
        if (this is Error) {
            action(error)
        }
        return this
    }

    inline fun <T> asSuccess(transform: (S) -> T): Result<T, E> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(error)
        }
    }
}

typealias CoreResult<T> = Result<T, Failure.CoreFailure>