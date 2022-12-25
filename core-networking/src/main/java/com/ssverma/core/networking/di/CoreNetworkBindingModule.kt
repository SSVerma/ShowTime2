package com.ssverma.core.networking.di

import com.ssverma.core.networking.RestClientImpl
import com.ssverma.core.networking.RestClient
import com.ssverma.core.networking.config.DefaultOkHttpConfig
import com.ssverma.core.networking.config.DefaultRetrofitConfig
import com.ssverma.core.networking.config.OkHttpConfig
import com.ssverma.core.networking.config.RetrofitConfig
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreNetworkBindingModule {

    @Singleton
    @Binds
    internal abstract fun bindRestClient(restClientImpl: RestClientImpl): RestClient

    @CoreNetworking
    @Singleton
    @Binds
    internal abstract fun bindOkHttpConfig(defaultOkHttpConfig: DefaultOkHttpConfig): OkHttpConfig

    @CoreNetworking
    @Singleton
    @Binds
    internal abstract fun bindRetrofitConfig(
        defaultRetrofitConfig: DefaultRetrofitConfig
    ): RetrofitConfig
}