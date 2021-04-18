package com.ssverma.showtime.api

import retrofit2.Response

enum class DataOrigin {
    Api,
    Local
}

abstract class Resource<out T> {
    open class Success<T>(
        open val data: T,
        val origin: DataOrigin
    ) : Resource<T>()

    data class Loading<T>(
        val cachedData: T? = null,
        val origin: DataOrigin = DataOrigin.Local
    ) : Resource<T>()

    open class Error<T>(
        open val displayErrorMessage: String,
        open val debugErrorMessage: String? = null
    ) : Resource<T>()
}

sealed class ApiResource<T> : Resource<T>() {
    data class Success<T>(
        override val data: T,
        val response: Response<T>
    ) : Resource.Success<T>(data = data, origin = DataOrigin.Api)

    data class Error<T, E>(
        override val displayErrorMessage: String,
        override val debugErrorMessage: String?,
        val response: Response<T>?,
        val errorPayload: E?,
        val retry: (() -> Unit)?
    ) : Resource.Error<T>(
        displayErrorMessage = displayErrorMessage,
        debugErrorMessage = debugErrorMessage
    )
}