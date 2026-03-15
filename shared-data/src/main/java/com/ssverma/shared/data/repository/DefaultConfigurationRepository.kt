package com.ssverma.shared.data.repository

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.shared.data.mapper.asDomainResult
import com.ssverma.shared.data.mapper.asLanguages
import com.ssverma.shared.data.mapper.asWatchProviderRegions
import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.domain.repository.ConfigurationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultConfigurationRepository @Inject constructor(
    private val tmdbApiService: TmdbApiService
) : ConfigurationRepository {

    private var cachedCountries: List<WatchProviderRegion>? = null
    private var cachedLanguages: List<Language>? = null

    override suspend fun fetchCountries(): CoreResult<List<WatchProviderRegion>> {
        cachedCountries?.let { return Result.Success(it) }

        val result = tmdbApiService.getCountries().asDomainResult { response ->
            response.body.asWatchProviderRegions()
        }

        if (result is Result.Success) {
            cachedCountries = result.data
        }

        return result
    }

    override suspend fun fetchLanguages(): CoreResult<List<Language>> {
        cachedLanguages?.let { return Result.Success(it) }

        val result = tmdbApiService.getLanguages().asDomainResult { response ->
            response.body.asLanguages()
        }

        if (result is Result.Success) {
            cachedLanguages = result.data
        }

        return result
    }
}
