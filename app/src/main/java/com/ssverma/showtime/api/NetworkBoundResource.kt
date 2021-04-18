package com.ssverma.showtime.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException

private suspend fun <T, E> makeApiRequest(
    apiCall: suspend () -> Response<T>,
    provideErrorMessage: (errorPayload: E?, errorBody: ResponseBody?) -> String,
    provideErrorPayload: (errorBody: ResponseBody?) -> E?,
    onRetryResult: ((Resource<T>) -> Unit)?,
    coroutineScope: CoroutineScope?
): Resource<T> {

    val onRetry: (() -> Unit)? = onRetryResult?.let {
        {
            it.invoke(Resource.Loading())
            coroutineScope?.launch {
                val retryResult = withContext(Dispatchers.IO) {
                    makeApiRequest(
                        apiCall = apiCall,
                        provideErrorMessage = provideErrorMessage,
                        provideErrorPayload = provideErrorPayload,
                        onRetryResult = onRetryResult,
                        coroutineScope = coroutineScope
                    )
                }
                it.invoke(retryResult)
            }
        }
    }

    try {

        val apiResponse = apiCall()

        if (!apiResponse.isSuccessful || apiResponse.body() == null) {
            val errorPayload = provideErrorPayload(apiResponse.errorBody())

            return ApiResource.Error(
                displayErrorMessage = provideErrorMessage(errorPayload, apiResponse.errorBody()),
                debugErrorMessage = apiResponse.errorBody()?.toString(),
                response = apiResponse,
                errorPayload = errorPayload,
                retry = onRetry
            )
        }

        return ApiResource.Success(data = apiResponse.body()!!, response = apiResponse)

    } catch (e: IOException) {
        e.printStackTrace()
        return ApiResource.Error<T, E>(
            displayErrorMessage = provideErrorMessage(null, null),
            debugErrorMessage = e.cause?.toString(),
            response = null,
            errorPayload = null,
            retry = onRetry
        )
    }
}


fun <T, E> makeApiRequest(
    coroutineScope: CoroutineScope,
    apiCall: suspend () -> Response<T>,
    provideErrorMessage: (errorPayload: E?, errorBody: ResponseBody?) -> String,
    provideErrorPayload: (errorBody: ResponseBody?) -> E?
): Flow<Resource<T>> {
    val result = MutableStateFlow<Resource<T>>(value = Resource.Loading())

    coroutineScope.launch {
        result.value = withContext(Dispatchers.IO) {
            makeApiRequest(
                apiCall = apiCall,
                provideErrorMessage = provideErrorMessage,
                provideErrorPayload = provideErrorPayload,
                coroutineScope = coroutineScope,
                onRetryResult = {
                    result.value = it
                }
            )
        }
    }

    return result
}

suspend fun <T, E> makeApiRequest(
    apiCall: suspend () -> Response<T>,
    provideErrorMessage: (errorPayload: E?, errorBody: ResponseBody?) -> String,
    provideErrorPayload: (errorBody: ResponseBody?) -> E?,
): Resource<T> {
    return makeApiRequest(
        apiCall = apiCall,
        provideErrorMessage = provideErrorMessage,
        provideErrorPayload = provideErrorPayload,
        onRetryResult = null,
        coroutineScope = null
    )
}