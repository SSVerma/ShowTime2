package com.ssverma.api.service.tmdb.interceptor

import com.ssverma.api.service.tmdb.di.TmdbServiceApiKey
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

internal class AuthInterceptor @Inject constructor(
    @TmdbServiceApiKey private val tmdbApiKey: String
) : ApplicationInterceptor {

    companion object {
        private const val QUERY_PARAM_API_KEY = "api_key"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url().newBuilder()
            .addQueryParameter(QUERY_PARAM_API_KEY, tmdbApiKey)
            .build()

        val request = original.newBuilder()
            .url(url)
            .build()

        return chain.proceed(request)
    }
}