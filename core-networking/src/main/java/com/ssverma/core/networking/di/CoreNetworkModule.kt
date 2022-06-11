package com.ssverma.core.networking.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssverma.core.networking.adapter.ApiResponseCallAdaptorFactory
import com.ssverma.core.networking.config.OkHttpConfig
import com.ssverma.core.networking.config.RetrofitConfig
import com.ssverma.core.networking.config.applyConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(includes = [CoreNetworkBindingModule::class])
@InstallIn(SingletonComponent::class)
internal object CoreNetworkModule {

    @Singleton
    @Provides
    internal fun provideRetrofitBuilder(
        @CoreNetworking retrofitConfig: RetrofitConfig,
        @CoreNetworking okHttpClient: OkHttpClient
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .applyConfig(retrofitConfig)
            .client(okHttpClient)
    }

    @CoreNetworking
    @Singleton
    @Provides
    internal fun provideGsonConvertorFactory(@CoreNetworking gson: Gson): GsonConverterFactory {
        return GsonConverterFactory.create(gson)
    }

    @CoreNetworking
    @Singleton
    @Provides
    internal fun provideGson(): Gson {
        return GsonBuilder()
            .setPrettyPrinting()
            .create()
    }

    @CoreNetworking
    @Singleton
    @Provides
    internal fun provideApiResponseCallAdapterFactory(): ApiResponseCallAdaptorFactory {
        return ApiResponseCallAdaptorFactory.create()
    }

    @CoreNetworking
    @Singleton
    @Provides
    internal fun provideHttpClient(@CoreNetworking okHttpConfig: OkHttpConfig): OkHttpClient {
        return OkHttpClient.Builder()
            .applyConfig(okHttpConfig)
            .build()
    }
}