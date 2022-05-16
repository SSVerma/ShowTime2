package com.ssverma.showtime.domain.usecase.tv

import com.ssverma.core.domain.failure.Failure
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.failure.tv.TvShowFailure
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.domain.repository.TvShowRepository
import com.ssverma.core.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TrendingTvShowsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : UseCase<TimeWindow, Result<List<TvShow>, Failure<TvShowFailure>>>(coroutineDispatcher) {

    override suspend fun execute(params: TimeWindow): Result<List<TvShow>, Failure<TvShowFailure>> {
        return tvShowRepository.fetchTrendingTvShows(timeWindow = params)
    }

}