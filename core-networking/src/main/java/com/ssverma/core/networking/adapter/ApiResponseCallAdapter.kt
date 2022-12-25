package com.ssverma.core.networking.adapter

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import java.lang.reflect.Type

internal class ApiResponseCallAdapter<S, E>(
    private val successType: Type,
    private val errorConvertor: Converter<ResponseBody, E>
) : CallAdapter<S, Call<ApiResponse<S, E>>> {

    override fun responseType(): Type {
        return successType
    }

    override fun adapt(call: Call<S>): Call<ApiResponse<S, E>> {
        return ApiResponseCall(call, errorConvertor)
    }
}