package com.ssverma.shared.domain.usecase

import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.repository.WatchProviderRepository
import javax.inject.Inject

class FetchAllWatchProvidersUseCase @Inject constructor(
    private val watchProviderRepository: WatchProviderRepository
) {
    suspend fun fetchMovieWatchProviders(): CoreResult<List<ProviderInfo>> {
        return watchProviderRepository.fetchAllMovieWatchProviders()
    }

    suspend fun fetchTvWatchProviders(): CoreResult<List<ProviderInfo>> {
        return watchProviderRepository.fetchAllTvShowWatchProviders()
    }
}
