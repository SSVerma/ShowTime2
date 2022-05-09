package com.ssverma.showtime.domain.usecase.tv

import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.defaults.tv.TvShowDefaults
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.domain.failure.tv.TvShowFailure
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.domain.repository.TvShowRepository
import com.ssverma.showtime.domain.usecase.NoParamUseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class UpcomingTvShowsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : NoParamUseCase<DomainResult<List<TvShow>, Failure<TvShowFailure>>>(coroutineDispatcher) {

    override suspend fun execute(): DomainResult<List<TvShow>, Failure<TvShowFailure>> {
        val tvConfig = TvShowDefaults.DiscoverDefaults.upcoming()
        return tvShowRepository.discoverTvShows(tvConfig)
    }
}