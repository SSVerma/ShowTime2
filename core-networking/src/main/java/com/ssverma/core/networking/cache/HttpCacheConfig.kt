package com.ssverma.core.networking.cache

import java.util.concurrent.TimeUnit

/**
 * Configuration options for HTTP response caching.
 *
 * @property maxAgeSeconds Default fallback TTL for fresh cached responses when override is forced.
 * @property maxStaleSeconds Maximum tolerated staleness when offline or stale cache is allowed. Default: 7 days.
 * @property cacheOnlySuccessfulGets If true, only successful 2xx HTTP GET responses are cached. Default: true.
 * @property overrideServerCacheControl If true, overrides server-provided Cache-Control globally. If false (default),
 * only requests with [HEADER_SHOWTIME_CACHE_MAX_AGE] or server-provided Cache-Control are cached.
 */
data class HttpCacheConfig(
    val maxAgeSeconds: Int = TimeUnit.MINUTES.toSeconds(30).toInt(),
    val maxStaleSeconds: Int = TimeUnit.DAYS.toSeconds(7).toInt(),
    val cacheOnlySuccessfulGets: Boolean = true,
    val overrideServerCacheControl: Boolean = false
) {
    companion object {
        const val HEADER_SHOWTIME_CACHE_MAX_AGE = "X-ShowTime-Cache-Max-Age"
        val Default = HttpCacheConfig()
    }
}
