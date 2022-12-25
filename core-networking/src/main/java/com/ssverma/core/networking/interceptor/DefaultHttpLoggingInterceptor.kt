package com.ssverma.core.networking.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultHttpLoggingInterceptor @Inject constructor() : NetworkInterceptor {
    private val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
        // Find a way to control it from app level configs, like it should be turned off for
        // release build variants.
        level = HttpLoggingInterceptor.Level.BODY
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return httpLoggingInterceptor.intercept(chain)
    }
}