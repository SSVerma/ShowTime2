package com.ssverma.shared.data.di

import com.ssverma.shared.data.repository.DefaultWatchProviderRepository
import com.ssverma.shared.domain.repository.WatchProviderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WatchProviderModule {
    @Binds
    @Singleton
    abstract fun bindWatchProviderRepository(
        defaultWatchProviderRepository: DefaultWatchProviderRepository
    ): WatchProviderRepository
}
