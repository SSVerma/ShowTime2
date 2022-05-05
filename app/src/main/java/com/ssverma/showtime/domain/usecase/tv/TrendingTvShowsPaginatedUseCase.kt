package com.ssverma.showtime.domain.usecase.tv

import androidx.paging.PagingData
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.domain.repository.TvShowRepository
import com.ssverma.showtime.domain.usecase.FlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class TrendingTvShowsPaginatedUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : FlowUseCase<TimeWindow, PagingData<TvShow>>(coroutineDispatcher) {

    override fun execute(params: TimeWindow): Flow<PagingData<TvShow>> {
        return tvShowRepository.fetchTrendingTvShowsGradually(timeWindow = params)
    }
}