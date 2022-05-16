package com.ssverma.showtime.domain.usecase.movie

import androidx.paging.PagingData
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.repository.MovieRepository
import com.ssverma.core.domain.usecase.FlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class TrendingPaginatedMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : FlowUseCase<TimeWindow, PagingData<Movie>>(coroutineDispatcher) {

    override fun execute(params: TimeWindow): Flow<PagingData<Movie>> {
        return movieRepository.fetchTrendingMoviesGradually(timeWindow = params)
    }
}