package com.ssverma.feature.tv.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.UseCase
import com.ssverma.feature.tv.domain.failure.TvEpisodeFailure
import com.ssverma.feature.tv.domain.model.TvEpisode
import com.ssverma.feature.tv.domain.model.TvEpisodeConfig
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TvEpisodeUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : UseCase<TvEpisodeConfig, Result<TvEpisode, Failure<TvEpisodeFailure>>>(coroutineDispatcher) {

    override suspend fun execute(
        params: TvEpisodeConfig
    ): Result<TvEpisode, Failure<TvEpisodeFailure>> {
        return tvShowRepository.fetchTvEpisodeDetails(
            tvEpisodeConfig = params
        )
    }
}