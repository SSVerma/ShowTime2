package com.ssverma.core.ccm.di

import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.core.ccm.firebase.FirebaseAppConfigProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CcmModule {

    @Binds
    @Singleton
    abstract fun bindAppConfigProvider(
        firebaseAppConfigProvider: FirebaseAppConfigProvider
    ): AppConfigProvider
}
