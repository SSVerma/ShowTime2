package com.ssverma.api.service.tmdb.di

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.interceptor.AuthInterceptor
import com.ssverma.core.networking.RestClient
import com.ssverma.core.networking.config.AdditionalServiceConfig
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.core.networking.service.ServiceEnvironment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Singleton
    @Provides
    internal fun provideEnvironment(
        @TmdbServiceBaseUrl baseUrl: String
    ): ServiceEnvironment<TmdbApiService> {
        return object : ServiceEnvironment<TmdbApiService> {
            override val baseUrl: String
                get() = baseUrl

            override val serviceClass: Class<TmdbApiService>
                get() = TmdbApiService::class.java
        }
    }

    @Singleton
    @Provides
    internal fun provideServiceConfig(authInterceptor: AuthInterceptor): AdditionalServiceConfig {
        return object : AdditionalServiceConfig() {
            override val applicationInterceptors: List<ApplicationInterceptor>
                get() = listOf(authInterceptor)
        }
    }

    @Singleton
    @Provides
    internal fun provideTmdbApiService(
        restClient: RestClient,
        serviceEnvironment: ServiceEnvironment<TmdbApiService>,
        additionalConfig: AdditionalServiceConfig
    ): TmdbApiService {
        return restClient.createService(
            environment = serviceEnvironment,
            serviceConfig = additionalConfig
        )
    }
}