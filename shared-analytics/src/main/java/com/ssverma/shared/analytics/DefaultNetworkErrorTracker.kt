package com.ssverma.shared.analytics

import com.ssverma.core.analytics.CrashReporter
import com.ssverma.core.networking.tracker.NetworkErrorTracker
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exception class representing remote server failures (HTTP 5xx).
 * Preserved by ProGuard so Crashlytics groups server outages clearly.
 */
class RemoteServerException(message: String) : Exception(message)

@Singleton
class DefaultNetworkErrorTracker @Inject constructor(
    private val crashReporter: CrashReporter
) : NetworkErrorTracker {

    override fun trackHttpError(
        endpoint: String,
        httpCode: Int,
        message: String?,
        durationMs: Long
    ) {
        // 1. Always append a diagnostic breadcrumb for Crashlytics session history
        crashReporter.logBreadcrumb(
            "HTTP $httpCode on $endpoint (${durationMs}ms): ${message.orEmpty()}"
        )

        // 2. Only record 5xx Server Errors as non-fatal exceptions (avoids spamming 4xx client errors)
        if (httpCode >= 500) {
            crashReporter.recordException(
                throwable = RemoteServerException("HTTP $httpCode on $endpoint: ${message.orEmpty()}"),
                attributes = mapOf(
                    "endpoint" to endpoint,
                    "http_code" to httpCode.toString(),
                    "duration_ms" to durationMs.toString()
                )
            )
        }
    }

    override fun trackNetworkException(
        endpoint: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        // Log connection drops / timeouts as diagnostic breadcrumbs (avoids spamming offline errors)
        crashReporter.logBreadcrumb(
            "Network failure on $endpoint (${durationMs}ms): ${throwable::class.java.simpleName} - ${throwable.message.orEmpty()}"
        )
    }
}
