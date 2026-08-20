package com.ssverma.shared.data.di

import com.ssverma.core.networking.RestClient
import com.ssverma.core.networking.service.ServiceEnvironment
import com.ssverma.shared.data.remote.TraktSyncService
import com.ssverma.shared.data.repository.TraktSyncRepositoryImpl
import com.ssverma.shared.domain.repository.TraktSyncRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TraktSyncNetworkModule {

    @Provides
    @Singleton
    fun provideTraktSyncService(restClient: RestClient): TraktSyncService {
        val environment = object : ServiceEnvironment<TraktSyncService> {
            override val baseUrl: String = "https://api.trakt.tv/"
            override val serviceClass: Class<TraktSyncService> = TraktSyncService::class.java
        }
        return restClient.createService(environment = environment)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TraktSyncRepositoryBindingModule {

    @Binds
    @Singleton
    abstract fun bindTraktSyncRepository(
        impl: TraktSyncRepositoryImpl
    ): TraktSyncRepository
}
