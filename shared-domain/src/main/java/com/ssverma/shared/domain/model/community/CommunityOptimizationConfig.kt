package com.ssverma.shared.domain.model.community

import java.util.concurrent.TimeUnit

/**
 * Single Source of Truth (SSOT) for Community caching, fetch limits, and Firebase read optimization.
 *
 * Designed to ensure ShowTime stays comfortably within the Firebase Free Spark Plan (50k daily reads)
 * while maintaining responsive, offline-first UI performance.
 */
object CommunityOptimizationConfig {
    /**
     * Default maximum number of trending discussions queried on the Home dashboard shelf.
     * Reduced from 10 to 5 to halve Firestore read consumption on home launches.
     */
    const val DEFAULT_TRENDING_DISCUSSIONS_LIMIT = 5L

    /**
     * Remote Config key for overriding trending discussions limit dynamically.
     */
    const val REMOTE_KEY_TRENDING_DISCUSSIONS_LIMIT = "remote_trending_discussions_limit"

    /**
     * Default Time-To-Live (TTL) for in-memory trending discussions cache (60 minutes).
     */
    val DEFAULT_TRENDING_DISCUSSIONS_CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(60)

    /**
     * Remote Config key for overriding trending discussions cache TTL (in minutes).
     */
    const val REMOTE_KEY_TRENDING_DISCUSSIONS_CACHE_TTL_MINUTES =
        "remote_trending_discussions_cache_ttl_minutes"

    /**
     * Default Time-To-Live (TTL) for in-memory Daily Poll cache (60 minutes).
     */
    val DEFAULT_DAILY_POLL_CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(60)

    /**
     * Remote Config key for overriding daily poll cache TTL (in minutes).
     */
    const val REMOTE_KEY_DAILY_POLL_CACHE_TTL_MINUTES = "remote_daily_poll_cache_ttl_minutes"
}
