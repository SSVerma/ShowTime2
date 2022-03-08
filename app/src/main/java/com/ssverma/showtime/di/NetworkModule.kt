package com.ssverma.showtime.di

import com.ssverma.api.service.tmdb.di.TmdbServiceApiKey
import com.ssverma.api.service.tmdb.di.TmdbServiceBaseUrl
import com.ssverma.core.networking.RestClient
import com.ssverma.core.networking.config.AdditionalServiceConfig
import com.ssverma.core.networking.service.ServiceEnvironment
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

    @Deprecated("use api.service")
    @Singleton
    @Provides
    fun provideEnvironment(): ServiceEnvironment<com.ssverma.showtime.api.TmdbApiService> {
        return object : ServiceEnvironment<com.ssverma.showtime.api.TmdbApiService> {
            override val baseUrl: String
                get() = BuildConfig.BASE_URL

            override val serviceClass: Class<com.ssverma.showtime.api.TmdbApiService>
                get() = com.ssverma.showtime.api.TmdbApiService::class.java
        }
    }

    @Deprecated("use api.service", replaceWith = ReplaceWith("provideTmdbApiService()"))
    @Singleton
    @Provides
    fun provideTmdbApiService(
        restClient: RestClient,
        serviceEnvironment: ServiceEnvironment<com.ssverma.showtime.api.TmdbApiService>,
        additionalConfig: AdditionalServiceConfig
    ): com.ssverma.showtime.api.TmdbApiService {
        return restClient.createService(
            environment = serviceEnvironment,
            serviceConfig = additionalConfig
        )
    }
}