package com.ssverma.api.service.tmdb.interceptor

import androidx.annotation.Keep
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.shared.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

@Keep
class LanguageInterceptor @Inject constructor(
    private val appConfigRepository: AppConfigRepository
) : ApplicationInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val isTranslationEnabled = runBlocking { appConfigRepository.isTranslationEnabled.first() }
        val contentLanguage = runBlocking { appConfigRepository.contentLanguage.first() }
        val originalLanguage = runBlocking { appConfigRepository.preferredOriginalLanguage.first() }

        val languageCode = if (isTranslationEnabled) {
            contentLanguage
        } else {
            "en-US"
        }

        val original = chain.request()
        val url = original.url
        val urlString = url.toString()
        val urlBuilder = url.newBuilder()
            .setQueryParameter("language", languageCode)

        val existingWithOriginalLanguage = url.queryParameter("with_original_language")
        if (existingWithOriginalLanguage != null) {
            if (existingWithOriginalLanguage.isBlank()) {
                urlBuilder.removeAllQueryParameters("with_original_language")
            }
        } else if (urlString.contains("discover/") && originalLanguage.isNotBlank()) {
            urlBuilder.setQueryParameter("with_original_language", originalLanguage)
        } else {
            urlBuilder.removeAllQueryParameters("with_original_language")
        }

        val request = original.newBuilder()
            .url(urlBuilder.build())
            .build()

        return chain.proceed(request)
    }
}
