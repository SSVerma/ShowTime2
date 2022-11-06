package com.ssverma.api.service.tmdb.interceptor

import com.ssverma.api.service.tmdb.di.TmdbServiceReadAccessToken
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

interface TmdbReadWriteAccessTokenProvider {
    suspend fun provideWriteAccessToken(): String
}

internal class AuthInterceptor @Inject constructor(
    @TmdbServiceReadAccessToken private val tmdbApiReadAccessToken: String,
    private val readWriteAccessTokenProvider: TmdbReadWriteAccessTokenProvider
) : ApplicationInterceptor {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER = "Bearer"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { readWriteAccessTokenProvider.provideWriteAccessToken() }.ifEmpty {
            tmdbApiReadAccessToken
        }

        val original = chain.request()
        val headers = original
            .headers().newBuilder()
            .add(HEADER_AUTHORIZATION, "$BEARER $token")
            .build()

        val request = original.newBuilder()
            .headers(headers)
            .build()

        return chain.proceed(request)
    }
}