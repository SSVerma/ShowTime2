package com.ssverma.core.networking.interceptor

import com.ssverma.core.networking.cache.HttpCacheConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Network interceptor that enables declarative and configurable HTTP response caching.
 *
 * Reads custom request header [HttpCacheConfig.HEADER_SHOWTIME_CACHE_MAX_AGE] to override
 * response Cache-Control header, while stripping the internal header from wire requests.
 * If no custom header is present, respects server-provided Cache-Control (unless [HttpCacheConfig.overrideServerCacheControl] is true).
 */
class ResponseCacheControlNetworkInterceptor(
    private val cacheConfig: HttpCacheConfig = HttpCacheConfig.Default
) : NetworkInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val customMaxAge =
            request.header(HttpCacheConfig.HEADER_SHOWTIME_CACHE_MAX_AGE)?.toIntOrNull()

        val requestToSend =
            if (request.header(HttpCacheConfig.HEADER_SHOWTIME_CACHE_MAX_AGE) != null) {
                request.newBuilder()
                    .removeHeader(HttpCacheConfig.HEADER_SHOWTIME_CACHE_MAX_AGE)
                    .build()
            } else {
                request
            }

        val originalResponse = chain.proceed(requestToSend)

        // Only cache GET requests if configured
        if (cacheConfig.cacheOnlySuccessfulGets && request.method != HttpMethod.GET) {
            return originalResponse
        }

        // Only cache successful 2xx responses
        if (cacheConfig.cacheOnlySuccessfulGets && !originalResponse.isSuccessful) {
            return originalResponse
        }

        // Respect explicit client bypass (e.g., Cache-Control: no-cache on request)
        val requestCacheControl = request.cacheControl
        if (requestCacheControl.noCache || requestCacheControl.noStore) {
            return originalResponse
        }

        val targetMaxAge = customMaxAge ?: if (cacheConfig.overrideServerCacheControl) {
            cacheConfig.maxAgeSeconds
        } else {
            null
        }

        if (targetMaxAge != null) {
            return originalResponse.newBuilder()
                .removeHeader(HEADER_PRAGMA)
                .removeHeader(HEADER_CACHE_CONTROL)
                .header(HEADER_CACHE_CONTROL, "public, max-age=$targetMaxAge")
                .build()
        }

        return originalResponse
    }

    companion object {
        private const val HEADER_CACHE_CONTROL = "Cache-Control"
        private const val HEADER_PRAGMA = "Pragma"
    }
}
