package com.ssverma.core.analytics

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local debug implementation of [CrashReporter] that logs diagnostics to Logcat.
 */
@Singleton
class DebugCrashReporter @Inject constructor() : CrashReporter {

    override fun recordException(throwable: Throwable, attributes: Map<String, String>) {
        val attrString = if (attributes.isNotEmpty()) " | attributes=$attributes" else ""
        Log.e(TAG, "Recorded Exception: ${throwable.message}$attrString", throwable)
    }

    override fun logBreadcrumb(message: String) {
        Log.d(TAG, "Breadcrumb: $message")
    }

    override fun setCustomKey(key: String, value: String) {
        Log.d(TAG, "CustomKey: $key = $value")
    }

    override fun setUserId(userId: String?) {
        Log.d(TAG, "UserId: $userId")
    }

    companion object {
        private const val TAG = "CrashReporter"
    }
}
