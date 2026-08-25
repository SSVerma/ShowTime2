package com.ssverma.core.networking.interceptor

import com.ssverma.core.networking.tracker.NetworkErrorTracker
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ErrorTrackingInterceptor @Inject constructor(
    private val networkErrorTracker: NetworkErrorTracker
) : ApplicationInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val endpoint = request.url.encodedPath
        val startNs = System.nanoTime()

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            val durationMs = (System.nanoTime() - startNs) / 1_000_000
            networkErrorTracker.trackNetworkException(
                endpoint = endpoint,
                throwable = e,
                durationMs = durationMs
            )
            throw e
        }

        val durationMs = (System.nanoTime() - startNs) / 1_000_000
        if (!response.isSuccessful) {
            networkErrorTracker.trackHttpError(
                endpoint = endpoint,
                httpCode = response.code,
                message = response.message,
                durationMs = durationMs
            )
        }

        return response
    }
}
