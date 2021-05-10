package com.ssverma.showtime.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ssverma.showtime.data.remote.response.TmdbErrorPayload
import com.ssverma.showtime.domain.ApiData
import com.ssverma.showtime.domain.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response

private const val DEFAULT_ERROR_MESSAGE = "Unknown Error"

private fun mapTmdbErrorPayload(errorBody: ResponseBody?): TmdbErrorPayload? {
    return errorBody?.let {
        try {
            Gson().fromJson(it.string(), TmdbErrorPayload::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }
}

suspend fun <T> makeTmdbApiRequest(
    apiCall: suspend () -> Response<T>,
    defaultErrorMessage: String = DEFAULT_ERROR_MESSAGE
): Result<ApiData<T>> {
    return makeApiRequest(
        apiCall = apiCall,
        provideErrorMessage = { errorPayload, _ ->
            errorPayload?.statusMessage ?: defaultErrorMessage
        },
        provideErrorPayload = {
            mapTmdbErrorPayload(it)
        },
    )
}

fun <T> makeTmdbApiRequest(
    coroutineScope: CoroutineScope = GlobalScope,
    defaultErrorMessage: String = DEFAULT_ERROR_MESSAGE,
    apiCall: suspend () -> Response<T>,
): Flow<Result<ApiData<T>>> {
    return makeApiRequest(
        apiCall = apiCall,
        coroutineScope = coroutineScope,
        provideErrorMessage = { errorPayload, _ ->
            errorPayload?.statusMessage ?: defaultErrorMessage
        },
        provideErrorPayload = {
            mapTmdbErrorPayload(it)
        }
    )
}

const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500" //TODO: Fetch from configs
