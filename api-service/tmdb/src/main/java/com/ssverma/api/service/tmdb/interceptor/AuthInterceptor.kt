package com.ssverma.api.service.tmdb.interceptor

import com.ssverma.api.service.tmdb.di.TmdbServiceApiReadAccessToken
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

internal class AuthInterceptor @Inject constructor(
    @TmdbServiceApiReadAccessToken private val tmdbApiReadAccessToken: String
) : ApplicationInterceptor {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER = "Bearer"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val headers = original
            .headers().newBuilder()
            .add(HEADER_AUTHORIZATION, "$BEARER $tmdbApiReadAccessToken")
            .build()

        val request = original.newBuilder()
            .headers(headers)
            .build()

        return chain.proceed(request)
    }
}