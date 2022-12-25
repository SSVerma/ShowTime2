package com.ssverma.shared.domain

import com.ssverma.shared.domain.failure.Failure

sealed class Result<out S, out E> {
    data class Success<S>(
        val data: S
    ) : Result<S, Nothing>()

    data class Error<E>(
        val error: E
    ) : Result<Nothing, E>()
}

typealias CoreResult<T> = Result<T, Failure.CoreFailure>