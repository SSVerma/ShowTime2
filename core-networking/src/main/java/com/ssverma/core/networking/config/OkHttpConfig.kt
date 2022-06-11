package com.ssverma.core.networking.config

import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.core.networking.interceptor.NetworkInterceptor
import okhttp3.Cache

/**
 * All basic sets of configurables properties of the OkHttpClient.
 * Subset of the properties client can configure directly.
 * @see [AdditionalServiceConfig]
 *
 */
internal interface OkHttpConfig {
    /**
     * Application level interceptors for OkHttpClient.
     * @see (https://square.github.io/okhttp/features/interceptors/#application-interceptors)
     */
    val applicationInterceptors: List<ApplicationInterceptor>

    /**
     * Network level interceptors for OkHttpClient.
     * @see (https://square.github.io/okhttp/features/interceptors/#network-interceptors)
     *
     */
    val networkInterceptors: List<NetworkInterceptor>

    /**
     * Sets the response cache to be used to read and write cached responses.
     */
    val cache: Cache?
}