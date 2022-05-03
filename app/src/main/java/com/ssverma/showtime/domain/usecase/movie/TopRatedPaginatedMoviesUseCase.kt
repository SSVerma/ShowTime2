package com.ssverma.showtime.domain.usecase.movie

import androidx.paging.PagingData
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.repository.MovieRepository
import com.ssverma.showtime.domain.usecase.NoParamFlowUseCase
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