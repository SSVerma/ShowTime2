package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProvider
import com.ssverma.shared.domain.model.WatchProviderRegion
import kotlinx.coroutines.flow.Flow

interface WatchProviderRepository {
    suspend fun fetchMovieWatchProviders(movieId: Int): CoreResult<WatchProvider?>

    suspend fun fetchTvShowWatchProviders(tvShowId: Int): CoreResult<WatchProvider?>

    suspend fun fetchAvailableWatchRegions(): CoreResult<List<WatchProviderRegion>>

    suspend fun fetchAvailableLanguages(): CoreResult<List<Language>>
}
