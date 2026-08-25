package com.ssverma.showtime.di

import com.ssverma.shared.domain.notifier.WidgetSyncNotifier
import com.ssverma.showtime.widget.AppWidgetSyncNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {

    @Binds
    @Singleton
    abstract fun bindWidgetSyncNotifier(
        impl: AppWidgetSyncNotifier
    ): WidgetSyncNotifier
}
