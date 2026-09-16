package com.ssverma.core.analytics

/**
 * Technical diagnostics and crash reporting contract for ShowTime.
 * Completely decoupled from user journey and product analytics.
 */
interface CrashReporter {
    /**
     * Records a non-fatal exception with optional contextual attributes.
     */
    fun recordException(throwable: Throwable, attributes: Map<String, String> = emptyMap())

    /**
     * Appends a diagnostic breadcrumb to provide historical context if a crash occurs later.
     */
    fun logBreadcrumb(message: String)

    /**
     * Attaches a key-value diagnostic metadata tag to the crash report context.
     */
    fun setCustomKey(key: String, value: String)

    /**
     * Sets an anonymized or technical user identifier for diagnostic grouping.
     */
    fun setUserId(userId: String?)
}
