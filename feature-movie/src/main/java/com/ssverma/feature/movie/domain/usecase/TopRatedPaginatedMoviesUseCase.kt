package com.ssverma.feature.movie.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.movie.domain.repository.MovieRepository
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.usecase.NoParamFlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class TopRatedPaginatedMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : NoParamFlowUseCase<PagingData<Movie>>(coroutineDispatcher) {

    override fun execute(): Flow<PagingData<Movie>> {
        return movieRepository.fetchTopRatedMoviesGradually()
    }
}