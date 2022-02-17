package com.ssverma.core.networking.adapter

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class ApiResponseCallAdaptorFactory private constructor() : CallAdapter.Factory() {
    companion object {
        fun create() = ApiResponseCallAdaptorFactory()
    }

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {

        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                "return type must be generic call " +
                        "i.e fun getExample(): Call<ApiResponse<S,E>> " +
                        "or suspend fun getExample(): ApiResponse<S, E>"
            )
        }

        val responseType = getParameterUpperBound(0, returnType)

        if (getRawType(responseType) != ApiResponse::class.java) {
            return null
        }

        if (responseType !is ParameterizedType) {
            throw IllegalStateException(
                "response type must be generic, ex: ApiResponse<S, E> " +
                        "i.e fun getExample(): Call<ApiResponse<S,E>> " +
                        "or suspend fun getExample(): ApiResponse<S, E>"
            )
        }

        val successType = getParameterUpperBound(0, responseType)
        val errorType = getParameterUpperBound(1, responseType)

        val errorBodyConvertor = retrofit
            .nextResponseBodyConverter<Any>(null, errorType, annotations)

        return ApiResponseCallAdapter<Any, Any>(
            successType = successType,
            errorConvertor = errorBodyConvertor
        )
    }
}