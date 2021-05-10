package com.ssverma.showtime.api.interceptor

import com.ssverma.showtime.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
            .build()

        val request = original.newBuilder()
            .url(url = url)
            .build()

        return chain.proceed(request = request)
    }
}