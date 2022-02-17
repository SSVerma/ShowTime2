package com.ssverma.core.networking.config

import com.ssverma.core.networking.adapter.ApiResponseCallAdaptorFactory
import com.ssverma.core.networking.di.CoreNetworking
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultRetrofitConfig @Inject constructor(
    @CoreNetworking private val gsonConverterFactory: GsonConverterFactory,
    @CoreNetworking private val apiResponseCallAdaptorFactory: ApiResponseCallAdaptorFactory
) : RetrofitConfig {
    override val annotatedConvertorFactories: Map<Class<out Annotation>, Converter.Factory>
        get() = emptyMap()

    override val convertorFactories: List<Converter.Factory>
        get() = listOf(gsonConverterFactory)

    override val callAdapterFactories: List<CallAdapter.Factory>
        get() = listOf(apiResponseCallAdaptorFactory)
}