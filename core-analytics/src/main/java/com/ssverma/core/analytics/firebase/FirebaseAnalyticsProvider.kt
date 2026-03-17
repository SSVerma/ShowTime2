package com.ssverma.core.analytics.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FirebaseAnalyticsProvider @Inject constructor(
    @ApplicationContext context: Context
) : AnalyticsProvider {

    private val firebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        // This tells the actual Firebase SDK to start/stop its background collection
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    override fun logEvent(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event.eventName, event.params.toBundle())
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}
