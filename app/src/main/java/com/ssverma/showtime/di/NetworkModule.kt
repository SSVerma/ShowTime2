package com.ssverma.showtime.di

import com.ssverma.core.networking.RestClient
import com.ssverma.core.networking.config.AdditionalServiceConfig
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.core.networking.service.ServiceEnvironment
import com.ssverma.showtime.BuildConfig
import com.ssverma.showtime.api.TmdbApiService
import com.ssverma.showtime.api.interceptor.AuthInterceptor
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
    fun provideEnvironment(): ServiceEnvironment<TmdbApiService> {
        return object : ServiceEnvironment<TmdbApiService> {
            override val baseUrl: String
                get() = BuildConfig.BASE_URL

            override val serviceClass: Class<TmdbApiService>
                get() = TmdbApiService::class.java
        }
    }

    @Singleton
    @Provides
    fun provideServiceConfig(authInterceptor: AuthInterceptor): AdditionalServiceConfig {
        return object : AdditionalServiceConfig() {
            override val applicationInterceptors: List<ApplicationInterceptor>
                get() = listOf(authInterceptor)
        }
    }

    @Singleton
    @Provides
    fun provideTmdbApiService(
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