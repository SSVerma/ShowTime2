package com.ssverma.api.service.tmdb.interceptor

import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.shared.domain.repository.AppConfigRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class RegionInterceptor @Inject constructor(
    private val appConfigRepository: AppConfigRepository
) : ApplicationInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val region = appConfigRepository.watchProviderRegion.value
        
        val original = chain.request()
        val url = original.url
        val urlString = url.toString()

        val newUrlBuilder = url.newBuilder()

        // Inject watch_region for discover APIs
        if (urlString.contains("discover/")) {
            if (url.queryParameter("watch_region") == null) {
                newUrlBuilder.addQueryParameter("watch_region", region)
            }
        } else {
            // Inject region for other APIs (movie, tv, search, etc.)
            // Note: Not all APIs support 'region', but it's generally safe as TMDB ignores unknown params
            if (url.queryParameter("region") == null) {
                newUrlBuilder.addQueryParameter("region", region)
            }
        }

        val request = original.newBuilder()
            .url(newUrlBuilder.build())
            .build()

        return chain.proceed(request)
    }
}
