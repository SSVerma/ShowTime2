package com.ssverma.feature.auth.di

import com.ssverma.feature.auth.data.local.AuthLocalDataSource
import com.ssverma.feature.auth.data.local.DefaultAuthLocalDataSource
import com.ssverma.feature.auth.data.remote.AuthRemoteDataSource
import com.ssverma.feature.auth.data.remote.DefaultAuthRemoteDataSource
import com.ssverma.feature.auth.data.repository.DefaultAuthRepository
import com.ssverma.feature.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AuthDataModule {
    @Binds
    abstract fun provideAuthLocalDataSource(
        defaultAuthLocalDataSource: DefaultAuthLocalDataSource
    ): AuthLocalDataSource

    @Binds
    abstract fun provideAuthRemoteDataSource(
        defaultAuthRemoteDataSource: DefaultAuthRemoteDataSource
    ): AuthRemoteDataSource

    @Binds
    abstract fun provideAuthRepository(
        defaultAuthRepository: DefaultAuthRepository
    ): AuthRepository
}