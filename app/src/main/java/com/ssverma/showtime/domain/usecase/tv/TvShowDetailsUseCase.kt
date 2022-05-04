package com.ssverma.showtime.domain.usecase.tv

import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.domain.failure.tv.TvShowFailure
import com.ssverma.showtime.domain.model.TvShow
import com.ssverma.showtime.domain.model.tv.TvShowDetailsConfig
import com.ssverma.showtime.domain.repository.TvShowRepository
import com.ssverma.showtime.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TvShowDetailsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : UseCase<TvShowDetailsConfig, DomainResult<TvShow, Failure<TvShowFailure>>>(coroutineDispatcher) {

    override suspend fun execute(
        params: TvShowDetailsConfig
    ): DomainResult<TvShow, Failure<TvShowFailure>> {
        return tvShowRepository.fetchTvShowDetails(detailsConfig = params)
    }
}