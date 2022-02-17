package com.ssverma.core.networking.config

import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.core.networking.interceptor.DefaultHttpLoggingInterceptor
import com.ssverma.core.networking.interceptor.NetworkInterceptor
import okhttp3.Cache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultOkHttpConfig @Inject constructor(
    private val defaultLoggingInterceptor: DefaultHttpLoggingInterceptor
) : OkHttpConfig {

    override val applicationInterceptors: List<ApplicationInterceptor>
        get() = emptyList()

    override val networkInterceptors: List<NetworkInterceptor>
        get() = listOf(defaultLoggingInterceptor)

    override val cache: Cache?
        get() = null
}