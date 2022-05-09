package com.ssverma.showtime.di

import com.ssverma.api.service.tmdb.di.TmdbServiceApiKey
import com.ssverma.api.service.tmdb.di.TmdbServiceBaseUrl
import com.ssverma.showtime.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {
    @TmdbServiceApiKey
    @Singleton
    @Provides
    fun provideTmdbApiKey(): String {
        return BuildConfig.TMDB_API_KEY
    }

    @TmdbServiceBaseUrl
    @Singleton
    @Provides
    fun provideTmdbServiceBaseUrl(): String {
        return BuildConfig.BASE_URL
    }
}