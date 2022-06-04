package com.ssverma.feature.tv.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.usecase.NoParamFlowUseCase
import com.ssverma.feature.tv.domain.model.TvShow
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class TopRatedTvShowsPaginatedUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : NoParamFlowUseCase<PagingData<TvShow>>(coroutineDispatcher) {

    override fun execute(): Flow<PagingData<TvShow>> {
        return tvShowRepository.fetchTopRatedTvShowsGradually()
    }
}