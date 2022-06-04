package com.ssverma.feature.tv.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.usecase.NoParamUseCase
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.domain.model.TvShow
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TopRatedTvShowsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : NoParamUseCase<Result<List<TvShow>, Failure<TvShowFailure>>>(coroutineDispatcher) {

    override suspend fun execute(): Result<List<TvShow>, Failure<TvShowFailure>> {
        return tvShowRepository.fetchTopRatedTvShows()
    }
}