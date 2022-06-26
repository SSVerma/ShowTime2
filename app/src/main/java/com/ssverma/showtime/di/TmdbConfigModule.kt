package com.ssverma.showtime.di

import com.ssverma.api.service.tmdb.di.TmdbServiceApiReadAccessToken
import com.ssverma.api.service.tmdb.di.TmdbServiceBaseUrl
import com.ssverma.showtime.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class TmdbConfigModule {
    @TmdbServiceApiReadAccessToken
    @Singleton
    @Provides
    fun provideTmdbApiKey(): String {
        return BuildConfig.TMDB_API_READ_ACCESS_TOKEN
    }

    @TmdbServiceBaseUrl
    @Singleton
    @Provides
    fun provideTmdbServiceBaseUrl(): String {
        return BuildConfig.TMDB_BASE_URL
    }
}