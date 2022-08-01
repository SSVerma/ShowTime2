package com.ssverma.api.service.tmdb.interceptor

import com.ssverma.api.service.tmdb.di.TmdbServiceReadAccessToken
import com.ssverma.api.service.tmdb.di.TmdbServiceReadWriteAccessToken
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

internal class AuthInterceptor @Inject constructor(
    @TmdbServiceReadAccessToken private val tmdbApiReadAccessToken: String,
    @TmdbServiceReadWriteAccessToken private val tmdbApiReadWriteAccessToken: String
) : ApplicationInterceptor {

    // TODO: Find a mechanism to instantly load read-write token after login
    // Currently it loaded after next app launch

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER = "Bearer"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tmdbApiReadWriteAccessToken.ifEmpty {
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