package com.ssverma.core.analytics

import android.util.Log
import javax.inject.Inject

class DebugAnalyticsProvider @Inject constructor() : AnalyticsProvider {

    override fun setCollectionEnabled(enabled: Boolean) {
        Log.d("Analytics", "Analytics collection enabled: $enabled")
    }

    override fun logEvent(event: AnalyticsEvent) {
        Log.d("Analytics", "Analytics Event: $event")
    }

    override fun setUserId(userId: String?) {
        Log.d("Analytics", "User ID set: $userId")
    }

    override fun setUserProperty(name: String, value: String?) {
        Log.d("Analytics", "User Property set: $name = $value")
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        Log.d("Analytics", "Screen View: $screenName, Class: $screenClass")
    }
}
