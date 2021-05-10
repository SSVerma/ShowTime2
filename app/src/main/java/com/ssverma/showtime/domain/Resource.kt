package com.ssverma.showtime.domain

import retrofit2.Response

enum class DataOrigin {
    Remote,
    Local
}

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

sealed class ApiData<T> {
    data class Success<T>(
        val data: T,
        val response: Response<T>
    ) : ApiData<T>()

    data class Error<T>(
        val response: Response<T>?
    ) : ApiData<T>()
}