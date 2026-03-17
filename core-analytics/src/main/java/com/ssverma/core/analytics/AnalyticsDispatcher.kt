package com.ssverma.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsDispatcher @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards AnalyticsProvider>
) : Analytics {

    private var isAnalyticsEnabled: Boolean = true

    fun setAnalyticsEnabled(enabled: Boolean) {
        this.isAnalyticsEnabled = enabled

        // Push the state down to Firebase (and any future providers like Mixpanel/Amplitude)
        providers.forEach { it.setCollectionEnabled(enabled) }
    }

    override fun logEvent(event: AnalyticsEvent) {
        if (!isAnalyticsEnabled) return

        providers.forEach { it.logEvent(event = event) }
    }

    override fun setUserId(userId: String?) {
        if (!isAnalyticsEnabled) return

        providers.forEach { it.setUserId(userId) }
    }

    override fun setUserProperty(name: String, value: String?) {
        if (!isAnalyticsEnabled) return

        providers.forEach { it.setUserProperty(name, value) }
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        if (!isAnalyticsEnabled) return

        providers.forEach { it.logScreenView(screenName, screenClass) }
    }
}
