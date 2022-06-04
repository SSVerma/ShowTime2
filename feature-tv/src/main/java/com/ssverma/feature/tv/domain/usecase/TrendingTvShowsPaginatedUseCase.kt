package com.ssverma.feature.tv.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.TimeWindow
import com.ssverma.core.domain.usecase.FlowUseCase
import com.ssverma.feature.tv.domain.model.TvShow
import com.ssverma.feature.tv.domain.repository.TvShowRepository
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