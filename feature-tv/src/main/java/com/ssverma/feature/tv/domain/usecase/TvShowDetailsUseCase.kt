package com.ssverma.feature.tv.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.domain.model.TvShowDetailsConfig
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TvShowDetailsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : UseCase<TvShowDetailsConfig, Result<TvShow, Failure<TvShowFailure>>>(coroutineDispatcher) {

    override suspend fun execute(
        params: TvShowDetailsConfig
    ): Result<TvShow, Failure<TvShowFailure>> {
        return tvShowRepository.fetchTvShowDetails(detailsConfig = params)
    }
}