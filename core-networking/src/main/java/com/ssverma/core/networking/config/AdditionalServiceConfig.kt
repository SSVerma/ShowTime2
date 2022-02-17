package com.ssverma.core.networking.config

import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.core.networking.interceptor.NetworkInterceptor
import okhttp3.Cache
import retrofit2.CallAdapter
import retrofit2.Converter

abstract class AdditionalServiceConfig : ServiceConfig {
    override val annotatedConvertorFactories: Map<Class<out Annotation>, Converter.Factory>
        get() = emptyMap()

    // Don't allow clients to provide convertor factories as order can't be determined
    // instead use annotatedConvertorFactories
    final override val convertorFactories: List<Converter.Factory>
        get() = emptyList()

    override val callAdapterFactories: List<CallAdapter.Factory>
        get() = emptyList()

    override val applicationInterceptors: List<ApplicationInterceptor>
        get() = emptyList()

    // Don't allow client to provide network interceptors
    final override val networkInterceptors: List<NetworkInterceptor>
        get() = emptyList()

    override val cache: Cache?
        get() = null
}