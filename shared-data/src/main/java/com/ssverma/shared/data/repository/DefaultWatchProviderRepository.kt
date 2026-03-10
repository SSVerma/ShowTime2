package com.ssverma.shared.data.repository

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.shared.data.mapper.asWatchProviderRegions
import com.ssverma.shared.data.mapper.asWatchProvidersMap
import com.ssverma.shared.data.mapper.asDomainResult
import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.Result
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
}
