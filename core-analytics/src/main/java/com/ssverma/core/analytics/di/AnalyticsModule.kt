package com.ssverma.core.analytics.di

import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.AnalyticsDispatcher
import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsProvider
import com.ssverma.core.analytics.BuildConfig
import com.ssverma.core.analytics.DebugAnalyticsProvider
import com.ssverma.core.analytics.firebase.FirebaseAnalyticsProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalytics(dispatcher: AnalyticsDispatcher): Analytics

    @Binds
    @IntoSet
    abstract fun bindDebugProvider(debugAnalyticsProvider: DebugAnalyticsProvider): AnalyticsProvider

    companion object {
        @Provides
        @IntoSet
        fun provideFirebaseProvider(
            firebaseAnalyticsProvider: FirebaseAnalyticsProvider
        ): AnalyticsProvider {
            return if (BuildConfig.DEBUG) {
                // In debug, we could either skip firebase or use a separate instance.
                // To avoid pollution:
                // We'll return a NoOp or just the debug provider.
                // Let's return a special wrapper or just skip adding it to the set.
//                NoOpAnalyticsProvider()
                DebugAnalyticsProvider()
            } else {
                firebaseAnalyticsProvider
            }
        }
    }
}

private class NoOpAnalyticsProvider : AnalyticsProvider {
    override fun setCollectionEnabled(enabled: Boolean) {}
    override fun logEvent(event: AnalyticsEvent) {}
    override fun setUserId(userId: String?) {}
    override fun setUserProperty(name: String, value: String?) {}
    override fun logScreenView(screenName: String, screenClass: String?) {}
}
