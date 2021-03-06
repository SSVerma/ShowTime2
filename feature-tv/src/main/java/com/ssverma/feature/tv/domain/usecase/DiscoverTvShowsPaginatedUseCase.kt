package com.ssverma.feature.tv.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.usecase.FlowUseCase
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