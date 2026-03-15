package com.ssverma.feature.tv.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.usecase.UseCase
import com.ssverma.feature.tv.domain.defaults.TvShowDefaults
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

import com.ssverma.shared.domain.model.DiscoveryParams

class PopularTvShowsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : UseCase<DiscoveryParams?, Result<List<TvShow>, Failure<TvShowFailure>>>(coroutineDispatcher) {

    override suspend fun execute(params: DiscoveryParams?): Result<List<TvShow>, Failure<TvShowFailure>> {
        val tvShowConfig = TvShowDefaults.DiscoverDefaults.popular(
            watchRegion = params?.region,
            originalLanguage = params?.originalLanguage
        )
        return tvShowRepository.discoverTvShows(tvShowConfig)
    }
}