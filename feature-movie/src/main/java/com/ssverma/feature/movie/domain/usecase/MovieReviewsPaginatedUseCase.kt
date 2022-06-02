package com.ssverma.feature.movie.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.model.Review
import com.ssverma.core.domain.usecase.FlowUseCase
import com.ssverma.feature.movie.domain.repository.MovieRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class MovieReviewsPaginatedUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : FlowUseCase<Int, PagingData<Review>>(coroutineDispatcher) {

    override fun execute(params: Int): Flow<PagingData<Review>> {
        return movieRepository.fetchMovieReviewsGradually(movieId = params)
    }
}