package com.ssverma.showtime.domain.usecase.tv

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.model.Review
import com.ssverma.core.domain.usecase.FlowUseCase
import com.ssverma.showtime.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class TvShowReviewsPaginatedUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : FlowUseCase<Int, PagingData<Review>>(coroutineDispatcher) {

    override fun execute(params: Int): Flow<PagingData<Review>> {
        return tvShowRepository.fetchTvShowReviewsGradually(tvShowId = params)
    }
}