package com.ssverma.showtime.domain.usecase.tv

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.NoParamUseCase
import com.ssverma.showtime.domain.defaults.tv.TvShowDefaults
import com.ssverma.showtime.domain.failure.tv.TvShowFailure
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TodayAiringTvShowsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : NoParamUseCase<Result<List<TvShow>, Failure<TvShowFailure>>>(coroutineDispatcher) {

    override suspend fun execute(): Result<List<TvShow>, Failure<TvShowFailure>> {
        val tvDiscoverConfig = TvShowDefaults.DiscoverDefaults.todayAiring()
        return tvShowRepository.discoverTvShows(tvDiscoverConfig)
    }
}