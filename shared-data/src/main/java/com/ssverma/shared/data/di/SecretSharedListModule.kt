package com.ssverma.shared.data.di

import com.ssverma.shared.data.repository.SecretSharedListRepositoryImpl
import com.ssverma.shared.domain.repository.SecretSharedListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecretSharedListModule {

    @Binds
    @Singleton
    abstract fun bindSecretSharedListRepository(
        secretSharedListRepositoryImpl: SecretSharedListRepositoryImpl
    ): SecretSharedListRepository
}
