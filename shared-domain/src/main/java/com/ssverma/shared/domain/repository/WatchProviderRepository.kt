package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.WatchProvider
import com.ssverma.shared.domain.model.WatchProviderRegion

interface WatchProviderRepository {
    suspend fun fetchMovieWatchProviders(movieId: Int): CoreResult<WatchProvider?>

    suspend fun fetchTvShowWatchProviders(tvShowId: Int): CoreResult<WatchProvider?>

    suspend fun fetchAvailableWatchRegions(): CoreResult<List<WatchProviderRegion>>

    suspend fun fetchAvailableLanguages(): CoreResult<List<Language>>

    suspend fun fetchAllMovieWatchProviders(): CoreResult<List<ProviderInfo>>

    suspend fun fetchAllTvShowWatchProviders(): CoreResult<List<ProviderInfo>>
}
