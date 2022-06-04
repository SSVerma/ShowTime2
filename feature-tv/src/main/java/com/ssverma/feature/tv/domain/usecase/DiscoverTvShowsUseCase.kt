package com.ssverma.feature.tv.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.TvDiscoverConfig
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.UseCase
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.domain.model.TvShow
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class DiscoverTvShowsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : UseCase<TvDiscoverConfig, Result<List<TvShow>, Failure<TvShowFailure>>>(
    coroutineDispatcher
) {

    override suspend fun execute(
        params: TvDiscoverConfig
    ): Result<List<TvShow>, Failure<TvShowFailure>> {
        return tvShowRepository.discoverTvShows(params)
    }
}