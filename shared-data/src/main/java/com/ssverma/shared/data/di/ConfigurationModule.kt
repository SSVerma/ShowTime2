package com.ssverma.shared.data.di

import com.ssverma.shared.data.repository.DefaultConfigurationRepository
import com.ssverma.shared.domain.repository.ConfigurationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConfigurationModule {
    @Binds
    @Singleton
    abstract fun bindConfigurationRepository(
        defaultConfigurationRepository: DefaultConfigurationRepository
    ): ConfigurationRepository
}
