package com.ssverma.showtime.domain.usecase.tv

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.UseCase
import com.ssverma.showtime.domain.failure.tv.TvSeasonFailure
import com.ssverma.showtime.domain.model.tv.TvSeason
import com.ssverma.showtime.domain.model.tv.TvSeasonConfig
import com.ssverma.showtime.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TvSeasonUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : UseCase<TvSeasonConfig, Result<TvSeason, Failure<TvSeasonFailure>>>(coroutineDispatcher) {

    override suspend fun execute(params: TvSeasonConfig): Result<TvSeason, Failure<TvSeasonFailure>> {
        return tvShowRepository.fetchTvSeasonDetails(seasonConfig = params)
    }
}