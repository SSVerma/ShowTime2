package com.ssverma.core.networking.interceptor

import com.ssverma.core.networking.cache.HttpCacheConfig
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Application interceptor that forces OkHttp to serve cached responses when offline.
 */
class OfflineCacheApplicationInterceptor(
    private val isNetworkAvailable: () -> Boolean,
    private val cacheConfig: HttpCacheConfig = HttpCacheConfig.Default
) : ApplicationInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (request.method == HttpMethod.GET && !isNetworkAvailable()) {
            val cacheControl = CacheControl.Builder()
                .onlyIfCached()
                .maxStale(cacheConfig.maxStaleSeconds, TimeUnit.SECONDS)
                .build()

            request = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
        }

        return chain.proceed(request)
    }
}
