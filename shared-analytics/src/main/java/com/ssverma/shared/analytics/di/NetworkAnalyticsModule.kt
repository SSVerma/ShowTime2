package com.ssverma.shared.analytics.di

import com.ssverma.core.networking.tracker.NetworkErrorTracker
import com.ssverma.shared.analytics.DefaultNetworkErrorTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkAnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindNetworkErrorTracker(
        defaultNetworkErrorTracker: DefaultNetworkErrorTracker
    ): NetworkErrorTracker
}
