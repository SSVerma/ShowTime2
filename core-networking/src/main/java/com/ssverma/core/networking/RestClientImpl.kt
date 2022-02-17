package com.ssverma.core.networking

import com.ssverma.core.networking.config.AdditionalServiceConfig
import com.ssverma.core.networking.config.applyConfig
import com.ssverma.core.networking.di.CoreNetworking
import com.ssverma.core.networking.service.ServiceEnvironment
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RestClientImpl @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder,
    @CoreNetworking private val okHttpClient: OkHttpClient
) : RestClient {
    override fun <T> createService(
        environment: ServiceEnvironment<T>,
        serviceConfig: AdditionalServiceConfig?
    ): T {

        retrofitBuilder
            .baseUrl(environment.baseUrl)

        serviceConfig?.let { config ->
            val updatedClient = okHttpClient
                .newBuilder()
                .applyConfig(config)
                .build()

            retrofitBuilder
                .applyConfig(config)
                .client(updatedClient)
        }

        return retrofitBuilder.build()
            .create(environment.serviceClass)
    }
}