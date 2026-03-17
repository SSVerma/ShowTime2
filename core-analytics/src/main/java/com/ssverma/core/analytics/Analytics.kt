package com.ssverma.core.analytics

interface Analytics {
    fun logEvent(event: AnalyticsEvent)

    fun setUserId(userId: String?)

    fun setUserProperty(name: String, value: String?)

    fun logScreenView(screenName: String, screenClass: String? = null)
}
