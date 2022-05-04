package com.ssverma.showtime.domain.usecase.tv

import androidx.paging.PagingData
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.TvDiscoverConfig
import com.ssverma.showtime.domain.model.TvShow
import com.ssverma.showtime.domain.repository.TvShowRepository
import com.ssverma.showtime.domain.usecase.FlowUseCase
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