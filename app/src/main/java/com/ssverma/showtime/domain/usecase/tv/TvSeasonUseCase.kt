package com.ssverma.showtime.domain.usecase.tv

import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.domain.failure.tv.TvSeasonFailure
import com.ssverma.showtime.domain.model.tv.TvSeason
import com.ssverma.showtime.domain.model.tv.TvSeasonConfig
import com.ssverma.showtime.domain.repository.TvShowRepository
import com.ssverma.showtime.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TvSeasonUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : UseCase<TvSeasonConfig, DomainResult<TvSeason, Failure<TvSeasonFailure>>>(coroutineDispatcher) {

    override suspend fun execute(params: TvSeasonConfig): DomainResult<TvSeason, Failure<TvSeasonFailure>> {
        return tvShowRepository.fetchTvSeasonDetails(seasonConfig = params)
    }
}