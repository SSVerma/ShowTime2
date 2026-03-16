package com.ssverma.shared.data.di

import com.ssverma.shared.data.repository.DefaultDiscoveryRepository
import com.ssverma.shared.domain.repository.DiscoveryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DiscoveryModule {

    @Binds
    @Singleton
    abstract fun bindDiscoveryRepository(
        defaultDiscoveryRepository: DefaultDiscoveryRepository
    ): DiscoveryRepository
}
