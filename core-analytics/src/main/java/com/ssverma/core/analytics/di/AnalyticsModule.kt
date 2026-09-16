package com.ssverma.core.analytics.di

import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.AnalyticsDispatcher
import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsProvider
import com.ssverma.core.analytics.BuildConfig
import com.ssverma.core.analytics.CrashReporter
import com.ssverma.core.analytics.DebugAnalyticsProvider
import com.ssverma.core.analytics.DebugCrashReporter
import com.ssverma.core.analytics.firebase.FirebaseAnalyticsProvider
import com.ssverma.core.analytics.firebase.FirebaseCrashReporter
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
            firebaseAnalyticsProvider: dagger.Lazy<FirebaseAnalyticsProvider>
        ): AnalyticsProvider {
            return if (BuildConfig.DEBUG) {
                NoOpAnalyticsProvider()
            } else {
                firebaseAnalyticsProvider.get()
            }
        }

        @Provides
        @Singleton
        fun provideCrashReporter(
            debugCrashReporter: DebugCrashReporter,
            firebaseCrashReporter: dagger.Lazy<FirebaseCrashReporter>
        ): CrashReporter {
            return if (BuildConfig.DEBUG) {
                debugCrashReporter
            } else {
                firebaseCrashReporter.get()
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
