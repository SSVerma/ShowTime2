package com.ssverma.showtime.domain.usecase.tv

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.UseCase
import com.ssverma.showtime.domain.failure.tv.TvShowFailure
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.domain.model.tv.TvShowDetailsConfig
import com.ssverma.showtime.domain.repository.TvShowRepository
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