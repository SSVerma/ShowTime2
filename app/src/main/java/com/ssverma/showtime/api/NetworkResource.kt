package com.ssverma.showtime.api

import com.ssverma.showtime.domain.ApiData
import com.ssverma.showtime.domain.DataOrigin
import com.ssverma.showtime.domain.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException

private suspend fun <S, E> makeApiRequest(
    apiCall: suspend () -> Response<S>,
    provideErrorMessage: (errorPayload: E?, errorBody: ResponseBody?) -> String,
    provideErrorPayload: (errorBody: ResponseBody?) -> E?,
    onRetryResult: ((Result<ApiData<S>>) -> Unit)?,
    coroutineScope: CoroutineScope?
): Result<ApiData<S>> {

    val onRetry: (() -> Unit)? = onRetryResult?.let {
        {
            it.invoke(Result.Loading())
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
        val responseBody = apiResponse.body()

        if (!apiResponse.isSuccessful || responseBody == null) {
            val errorPayload = provideErrorPayload(apiResponse.errorBody())

            return Result.Error(
                displayMessage = provideErrorMessage(errorPayload, apiResponse.errorBody()),
                cause = IOException("Api request unsuccessful"),
                payload = errorPayload,
                retry = onRetry
            )
        }

        return Result.Success(
            data = ApiData.Success(
                data = responseBody,
                response = apiResponse
            ),
            origin = DataOrigin.Remote
        )

    } catch (e: IOException) {
        e.printStackTrace()
        return Result.Error(
            displayMessage = provideErrorMessage(null, null),
            cause = e,
            retry = onRetry
        )
    }
}


fun <S, E> makeApiRequest(
    coroutineScope: CoroutineScope,
    apiCall: suspend () -> Response<S>,
    provideErrorMessage: (errorPayload: E?, errorBody: ResponseBody?) -> String,
    provideErrorPayload: (errorBody: ResponseBody?) -> E?
): Flow<Result<ApiData<S>>> {
    val data: MutableStateFlow<Result<ApiData<S>>> = MutableStateFlow(value = Result.Loading())

    coroutineScope.launch {
        data.value = withContext(Dispatchers.IO) {
            makeApiRequest(
                apiCall = apiCall,
                provideErrorMessage = provideErrorMessage,
                provideErrorPayload = provideErrorPayload,
                coroutineScope = coroutineScope,
                onRetryResult = {
                    data.value = it
                }
            )
        }
    }

    return data
}

suspend fun <S, E> makeApiRequest(
    apiCall: suspend () -> Response<S>,
    provideErrorMessage: (errorPayload: E?, errorBody: ResponseBody?) -> String,
    provideErrorPayload: (errorBody: ResponseBody?) -> E?,
): Result<ApiData<S>> {
    return makeApiRequest(
        apiCall = apiCall,
        provideErrorMessage = provideErrorMessage,
        provideErrorPayload = provideErrorPayload,
        onRetryResult = null,
        coroutineScope = null
    )
}