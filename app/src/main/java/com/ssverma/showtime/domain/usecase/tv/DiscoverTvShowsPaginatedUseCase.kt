package com.ssverma.showtime.domain.usecase.tv

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.TvDiscoverConfig
import com.ssverma.core.domain.usecase.FlowUseCase
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class DiscoverTvShowsPaginatedUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : FlowUseCase<TvDiscoverConfig, PagingData<TvShow>>(coroutineDispatcher) {

    override fun execute(params: TvDiscoverConfig): Flow<PagingData<TvShow>> {
        return tvShowRepository.discoverTvShowsGradually(
            discoverConfig = params
        )
    }
}