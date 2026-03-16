package com.ssverma.shared.data.repository

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.shared.data.mapper.asDomainResult
import com.ssverma.shared.data.mapper.asLanguages
import com.ssverma.shared.data.mapper.asProviderInfos
import com.ssverma.shared.data.mapper.asWatchProviderRegions
import com.ssverma.shared.data.mapper.asWatchProvidersMap
import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.WatchProvider
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.WatchProviderRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DefaultWatchProviderRepository @Inject constructor(
    private val tmdbApiService: TmdbApiService,
    private val appConfigRepository: AppConfigRepository
) : WatchProviderRepository {

    private var cachedRegions: List<WatchProviderRegion>? = null
    private var cachedLanguages: List<Language>? = null

    private var cachedMovieProviders: List<ProviderInfo>? = null

    private var cachedTvProviders: List<ProviderInfo>? = null

    override suspend fun fetchMovieWatchProviders(movieId: Int): CoreResult<WatchProvider?> {
        val region = appConfigRepository.watchProviderRegion.first()
        return tmdbApiService.getMovieWatchProviders(movieId).asDomainResult { response ->
            response.body.asWatchProvidersMap()[region]
        }
    }

    override suspend fun fetchTvShowWatchProviders(tvShowId: Int): CoreResult<WatchProvider?> {
        val region = appConfigRepository.watchProviderRegion.first()
        return tmdbApiService.getTvShowWatchProviders(tvShowId).asDomainResult { response ->
            response.body.asWatchProvidersMap()[region]
        }
    }

    override suspend fun fetchAvailableWatchRegions(): CoreResult<List<WatchProviderRegion>> {
        cachedRegions?.let {
            return Result.Success(it)
        }

        val result = tmdbApiService.getWatchProviderRegions().asDomainResult { response ->
            response.body.results.asWatchProviderRegions()
        }

        if (result is Result.Success) {
            cachedRegions = result.data
        }

        return result
    }

    override suspend fun fetchAvailableLanguages(): CoreResult<List<Language>> {
        cachedLanguages?.let {
            return Result.Success(it)
        }

        val result = tmdbApiService.getLanguages().asDomainResult { response ->
            response.body.asLanguages()
        }

        if (result is Result.Success) {
            cachedLanguages = result.data
        }

        return result
    }

    override suspend fun fetchAllMovieWatchProviders(): CoreResult<List<ProviderInfo>> {
        cachedMovieProviders?.let {
            return Result.Success(it)
        }

        val region = appConfigRepository.watchProviderRegion.first()

        val result = tmdbApiService.getAllMovieWatchProviders(watchRegion = region)
            .asDomainResult { response ->
                response.body.asProviderInfos()
            }

        if (result is Result.Success) {
            cachedMovieProviders = result.data
        }

        return result
    }

    override suspend fun fetchAllTvShowWatchProviders(): CoreResult<List<ProviderInfo>> {
        cachedTvProviders?.let {
            return Result.Success(it)
        }

        val region = appConfigRepository.watchProviderRegion.first()

        val result = tmdbApiService.getAllTvWatchProviders(region).asDomainResult { response ->
            response.body.asProviderInfos()
        }

        if (result is Result.Success) {
            cachedTvProviders = result.data
        }

        return result
    }

    override fun invalidateCache() {
        cachedMovieProviders = null
        cachedTvProviders = null
    }
}
