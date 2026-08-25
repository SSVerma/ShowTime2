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
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.util.concurrent.TimeUnit
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
    internal fun provideDns(): Dns {
        val bootstrapClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val doh = DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url("https://1.1.1.1/dns-query".toHttpUrl())
            .bootstrapDnsHosts(
                InetAddress.getByName("1.1.1.1"),
                InetAddress.getByName("1.0.0.1"),
                InetAddress.getByName("8.8.8.8"),
                InetAddress.getByName("8.8.4.4"),
                InetAddress.getByName("2606:4700:4700::1111"),
                InetAddress.getByName("2606:4700:4700::1001"),
                InetAddress.getByName("2001:4860:4860::8888"),
                InetAddress.getByName("2001:4860:4860::8844")
            )
            .includeIPv6(true)
            .build()

        return object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                return try {
                    val addresses = doh.lookup(hostname)
                    if (addresses.isNotEmpty()) addresses else Dns.SYSTEM.lookup(hostname)
                } catch (e: Exception) {
                    Dns.SYSTEM.lookup(hostname)
                }
            }
        }
    }

    @CoreNetworking
    @Singleton
    @Provides
    internal fun provideHttpClient(
        @CoreNetworking okHttpConfig: OkHttpConfig,
        @CoreNetworking dns: Dns
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .dns(dns)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .applyConfig(okHttpConfig)
            .build()
    }
}