package com.ssverma.showtime.domain.usecase.tv

import androidx.paging.PagingData
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.domain.repository.TvShowRepository
import com.ssverma.showtime.domain.usecase.NoParamFlowUseCase
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