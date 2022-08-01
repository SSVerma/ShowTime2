package com.ssverma.feature.auth.di

import com.ssverma.api.service.tmdb.di.TmdbServiceReadWriteAccessToken
import com.ssverma.feature.auth.data.local.AuthLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TmdbConfigModule {

    @TmdbServiceReadWriteAccessToken
    @Singleton
    @Provides
    fun provideTmdbApiKey(authLocalDataSource: AuthLocalDataSource): String = runBlocking {
        return@runBlocking authLocalDataSource.loadAccessToken()
    }
}