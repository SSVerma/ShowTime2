package com.ssverma.core.networking.adapter

import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

internal class ApiResponseCall<S, E>(
    private val call: Call<S>,
    private val errorConvertor: Converter<ResponseBody, E>
) : Call<ApiResponse<S, E>> {
    override fun clone(): Call<ApiResponse<S, E>> {
        return ApiResponseCall(call.clone(), errorConvertor)
    }

    override fun execute(): Response<ApiResponse<S, E>> {
        val response = call.execute()
        val apiResponse = response.asApiResponse(errorConvertor)
        return Response.success(apiResponse)
    }

    override fun enqueue(callback: Callback<ApiResponse<S, E>>) {
        call.enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val apiResponse = response.asApiResponse(errorConvertor)
                callback.onResponse(this@ApiResponseCall, Response.success(apiResponse))
            }

            override fun onFailure(call: Call<S>, throwable: Throwable) {
                val apiResponse = when (throwable) {
                    is ConnectException, is UnknownHostException -> {
                        ApiResponse.Error.NetworkError(throwable)
                    }
                    else -> {
                        ApiResponse.Error.UnexpectedError(throwable)
                    }
                }
                callback.onResponse(
                    this@ApiResponseCall,
                    Response.success(apiResponse)
                )
            }
        })
    }

    override fun isExecuted(): Boolean {
        return call.isExecuted
    }

    override fun cancel() {
        call.cancel()
    }

    override fun isCanceled(): Boolean {
        return call.isCanceled
    }

    override fun request(): Request {
        return call.request()
    }

    override fun timeout(): Timeout {
        return call.timeout()
    }

    private fun <R, E> Response<R>.asApiResponse(
        errorConvertor: Converter<ResponseBody, E>
    ): ApiResponse<R, E> {
        val code = this.code()
        val responseBody = this.body()
        val errorBody = this.errorBody()

        val payload = ResponsePayload(
            httpCode = this.code(),
            statusMessage = this.message(),
            headers = this.headers(),
            row = this.raw()
        )

        return when {
            code in 200 until 300 && responseBody != null -> {
                ApiResponse.Success(
                    body = responseBody,
                    payload = payload
                )
            }

            code in 400 until 500 -> {
                val convertedError = try {
                    errorBody?.let { errorConvertor.convert(it) }
                } catch (e: IOException) {
                    null
                }

                ApiResponse.Error.ClientError(
                    body = convertedError,
                    payload = payload
                )
            }

            code in 500 until 600 -> {
                ApiResponse.Error.ServerError(
                    payload = payload
                )
            }

            else -> {
                ApiResponse.Error.UnexpectedError(
                    throwable = RuntimeException("Unexpected response: $this")
                )
            }
        }
    }
}