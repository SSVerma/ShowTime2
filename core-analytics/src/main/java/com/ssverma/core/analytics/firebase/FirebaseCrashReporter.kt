package com.ssverma.core.analytics.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ssverma.core.analytics.CrashReporter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production Firebase Crashlytics implementation of [CrashReporter].
 */
@Singleton
class FirebaseCrashReporter @Inject constructor() : CrashReporter {

    private val crashlytics: FirebaseCrashlytics? by lazy {
        try {
            FirebaseCrashlytics.getInstance()
        } catch (_: Throwable) {
            null
        }
    }

    override fun recordException(throwable: Throwable, attributes: Map<String, String>) {
        crashlytics?.let { cl ->
            attributes.forEach { (key, value) ->
                cl.setCustomKey(key, value)
            }
            cl.recordException(throwable)
        }
    }

    override fun logBreadcrumb(message: String) {
        crashlytics?.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics?.setCustomKey(key, value)
    }

    override fun setUserId(userId: String?) {
        userId?.let { crashlytics?.setUserId(it) }
    }
}
