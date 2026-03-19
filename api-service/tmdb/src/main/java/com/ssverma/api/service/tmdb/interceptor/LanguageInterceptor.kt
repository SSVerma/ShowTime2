package com.ssverma.api.service.tmdb.interceptor

import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.shared.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

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
        val url = original.url.newBuilder()
            .setQueryParameter("language", languageCode)
            .setQueryParameter("with_original_language", originalLanguage)
            .build()

        val request = original.newBuilder()
            .url(url)
            .build()

        return chain.proceed(request)
    }
}
