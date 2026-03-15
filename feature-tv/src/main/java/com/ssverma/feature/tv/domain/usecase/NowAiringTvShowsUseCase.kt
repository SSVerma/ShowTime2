package com.ssverma.feature.tv.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.tv.domain.defaults.TvShowDefaults
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.usecase.NoParamUseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class NowAiringTvShowsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository,
    private val appConfigRepository: AppConfigRepository
) : NoParamUseCase<Result<List<TvShow>, Failure<TvShowFailure>>>(coroutineDispatcher) {

    override suspend fun execute(): Result<List<TvShow>, Failure<TvShowFailure>> {
        val watchRegion = appConfigRepository.watchProviderRegion.value
        val tvDiscoverConfig = TvShowDefaults.DiscoverDefaults.nowAiring(watchRegion)
        return tvShowRepository.discoverTvShows(tvDiscoverConfig)
    }
}