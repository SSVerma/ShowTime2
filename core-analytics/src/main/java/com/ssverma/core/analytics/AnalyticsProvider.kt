package com.ssverma.core.analytics

interface AnalyticsProvider {

    fun setCollectionEnabled(enabled: Boolean)

    fun logEvent(event: AnalyticsEvent)

    fun setUserId(userId: String?)

    fun setUserProperty(name: String, value: String?)

    fun logScreenView(screenName: String, screenClass: String?)
}
