package com.ssverma.feature.auth.di

import com.ssverma.core.networking.RestClient
import com.ssverma.core.networking.service.ServiceEnvironment
import com.ssverma.feature.auth.data.remote.TraktAuthService
import com.ssverma.feature.auth.domain.defaults.TraktDefaults
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TraktAuthNetworkModule {

    @Provides
    @Singleton
    fun provideTraktAuthService(restClient: RestClient): TraktAuthService {
        val environment = object : ServiceEnvironment<TraktAuthService> {
            override val baseUrl: String = TraktDefaults.BaseUrl
            override val serviceClass: Class<TraktAuthService> = TraktAuthService::class.java
        }
        return restClient.createService(environment = environment)
    }
}
