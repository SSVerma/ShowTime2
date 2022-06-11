package com.ssverma.feature.tv.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.usecase.UseCase
import com.ssverma.feature.tv.domain.failure.TvSeasonFailure
import com.ssverma.feature.tv.domain.model.TvSeason
import com.ssverma.feature.tv.domain.model.TvSeasonConfig
import com.ssverma.feature.tv.domain.repository.TvShowRepository
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