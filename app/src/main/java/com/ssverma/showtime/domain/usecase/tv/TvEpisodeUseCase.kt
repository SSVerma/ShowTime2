package com.ssverma.showtime.domain.usecase.tv

import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.domain.failure.tv.TvEpisodeFailure
import com.ssverma.showtime.domain.model.tv.TvEpisode
import com.ssverma.showtime.domain.model.tv.TvEpisodeConfig
import com.ssverma.showtime.domain.repository.TvShowRepository
import com.ssverma.showtime.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TvEpisodeUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : UseCase<TvEpisodeConfig, DomainResult<TvEpisode, Failure<TvEpisodeFailure>>>(coroutineDispatcher) {

    override suspend fun execute(
        params: TvEpisodeConfig
    ): DomainResult<TvEpisode, Failure<TvEpisodeFailure>> {
        return tvShowRepository.fetchTvEpisodeDetails(
            tvEpisodeConfig = params
        )
    }
}