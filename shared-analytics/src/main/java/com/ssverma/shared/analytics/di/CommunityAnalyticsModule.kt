package com.ssverma.shared.analytics.di

import com.ssverma.shared.analytics.community.DefaultFirestoreAuditTracker
import com.ssverma.shared.analytics.community.FirestoreAuditTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommunityAnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindFirestoreAuditTracker(
        defaultFirestoreAuditTracker: DefaultFirestoreAuditTracker
    ): FirestoreAuditTracker
}
