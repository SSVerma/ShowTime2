package com.ssverma.core.notifications.di

import com.ssverma.core.notifications.NotificationDispatcher
import com.ssverma.core.notifications.NotificationProvider
import com.ssverma.core.notifications.Notifications
import com.ssverma.core.notifications.firebase.FirebaseNotificationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindNotifications(dispatcher: NotificationDispatcher): Notifications

    @Binds
    @IntoSet
    abstract fun bindFirebaseProvider(
        firebaseNotificationProvider: FirebaseNotificationProvider
    ): NotificationProvider
}
