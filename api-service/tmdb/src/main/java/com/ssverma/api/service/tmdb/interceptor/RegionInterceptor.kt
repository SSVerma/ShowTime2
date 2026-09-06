package com.ssverma.api.service.tmdb.interceptor

import androidx.annotation.Keep
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.shared.domain.repository.AppConfigRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

@Keep
class RegionInterceptor @Inject constructor(
    private val appConfigRepository: AppConfigRepository
) : ApplicationInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val region = appConfigRepository.watchProviderRegion.value
        val original = chain.request()
        val url = original.url
        val urlString = url.toString()

        val newUrlBuilder = url.newBuilder()

        if (urlString.contains("discover/")) {
            // For discover APIs, only inject watch_region if not already provided and non-blank
            val existingWatchRegion = url.queryParameter("watch_region")
            if (existingWatchRegion == null && region.isNotBlank()) {
                newUrlBuilder.setQueryParameter("watch_region", region)
            } else if (existingWatchRegion != null && existingWatchRegion.isBlank()) {
                newUrlBuilder.removeAllQueryParameters("watch_region")
            }
        } else {
            // Inject region for other APIs (movie, tv, search, etc.)
            val existingRegion = url.queryParameter("region")
            if (existingRegion == null && region.isNotBlank()) {
                newUrlBuilder.setQueryParameter("region", region)
            } else if (existingRegion != null && existingRegion.isBlank()) {
                newUrlBuilder.removeAllQueryParameters("region")
            }
        }

        val request = original.newBuilder()
            .url(newUrlBuilder.build())
            .build()

        return chain.proceed(request)
    }
}
