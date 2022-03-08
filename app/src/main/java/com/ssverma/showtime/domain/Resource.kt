package com.ssverma.showtime.domain

import retrofit2.Response

enum class DataOrigin {
    Remote,
    Local
}

@Deprecated("use DomainResult")
sealed class Result<T> {
    data class Success<T>(
        val data: T,
        val origin: DataOrigin
    ) : Result<T>()

    data class Loading<T>(
        val cachedData: T? = null
    ) : Result<T>()

    data class Error<T>(
        val displayMessage: String,
        val cause: Throwable,
        val payload: Any? = null,
        val retry: (() -> Unit)? = null
    ) : Result<T>()
}

data class ApiData<T>(
    val payload: T,
    val response: Response<T>
)

//TODO: Rename as 'Result' once migration completes.
sealed class DomainResult<out S, out E> {
    data class Success<S>(
        val data: S
    ) : DomainResult<S, Nothing>()

    data class Error<E>(
        val error: E
    ) : DomainResult<Nothing, E>()
}