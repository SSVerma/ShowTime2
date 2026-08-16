package com.ssverma.showtime.di

import android.content.Context
import com.ssverma.api.service.tmdb.di.TmdbServiceBaseUrl
import com.ssverma.api.service.tmdb.di.TmdbServiceCache
import com.ssverma.api.service.tmdb.di.TmdbServiceReadAccessToken
import com.ssverma.showtime.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import java.io.File
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class TmdbConfigModule {
    @TmdbServiceReadAccessToken
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

    @TmdbServiceCache
    @Singleton
    @Provides
    fun provideTmdbCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "tmdb_http_cache")
        val cacheSize = 25L * 1024 * 1024 // 25 MB
        return Cache(cacheDir, cacheSize)
    }
}
