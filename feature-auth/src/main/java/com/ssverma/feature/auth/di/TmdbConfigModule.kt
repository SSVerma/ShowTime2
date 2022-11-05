package com.ssverma.feature.auth.di

import com.ssverma.api.service.tmdb.interceptor.TmdbReadWriteAccessTokenProvider
import com.ssverma.feature.auth.data.local.AuthLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TmdbConfigModule {

    @Singleton
    @Provides
    fun provideTmdbWriteAccessToken(
        authLocalDataSource: AuthLocalDataSource
    ): TmdbReadWriteAccessTokenProvider {
        return object : TmdbReadWriteAccessTokenProvider {
            override suspend fun provideWriteAccessToken(): String {
                return authLocalDataSource.loadAccessToken()
            }
        }
    }
}