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

    /**
     * Default maximum number of comments fetched on initial open of a discussion screen.
     * Reduced to 30 to conserve reads on the Firebase Spark plan while showing full conversation.
     */
    const val DEFAULT_DISCUSSION_COMMENTS_LIMIT = 30L

    /**
     * Remote Config key for overriding initial discussion comments limit dynamically.
     */
    const val REMOTE_KEY_DISCUSSION_COMMENTS_LIMIT = "remote_discussion_comments_limit"

    /**
     * Default Time-To-Live (TTL) for in-memory thread session cache (5 minutes).
     * Prevents redundant full-table queries when a user quickly re-opens a discussion.
     */
    val DEFAULT_DISCUSSION_SESSION_CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5)

    /**
     * Remote Config key for overriding thread session cache TTL (in minutes).
     */
    const val REMOTE_KEY_DISCUSSION_SESSION_CACHE_TTL_MINUTES =
        "remote_discussion_session_cache_ttl_minutes"

    /**
     * Default batch size when loading older comments via cursor pagination.
     */
    const val DEFAULT_DISCUSSION_PAGE_SIZE = 25L

    /**
     * Remote Config key for overriding discussion pagination page size.
     */
    const val REMOTE_KEY_DISCUSSION_PAGE_SIZE = "remote_discussion_page_size"

    /**
     * Default maximum number of comments fetched for the preview section on media details screens.
     * Keeps Details screen browsing reads minimal (max 2 comments per movie/show view).
     */
    const val DEFAULT_DISCUSSION_PREVIEW_LIMIT = 2L

    /**
     * Remote Config key for overriding discussion preview limit dynamically.
     */
    const val REMOTE_KEY_DISCUSSION_PREVIEW_LIMIT = "remote_discussion_preview_limit"

    /**
     * Default Time-To-Live (TTL) for in-memory media reactions session cache (15 minutes).
     * Eliminates redundant Firestore reads on rapid media details re-opening.
     */
    val DEFAULT_REACTIONS_CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(15)

    /**
     * Remote Config key for overriding media reactions cache TTL (in minutes).
     */
    const val REMOTE_KEY_REACTIONS_CACHE_TTL_MINUTES = "remote_reactions_cache_ttl_minutes"

    /**
     * Default flag for enabling Firestore query telemetry and quota audit tracking.
     * Enabled by default (true) to track production query metrics with zero UI/performance overhead.
     */
    const val DEFAULT_FIRESTORE_AUDIT_ENABLED = true

    /**
     * Remote Config key for dynamically enabling/disabling Firestore telemetry kill-switch.
     */
    const val REMOTE_KEY_FIRESTORE_AUDIT_ENABLED = "firestore_audit_enabled"
}
