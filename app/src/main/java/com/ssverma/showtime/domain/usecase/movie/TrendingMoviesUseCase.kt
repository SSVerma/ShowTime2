package com.ssverma.showtime.domain.usecase.movie

import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.domain.failure.movie.MovieFailure
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.repository.MovieRepository
import com.ssverma.showtime.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TrendingMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : UseCase<TimeWindow, DomainResult<List<Movie>, Failure<MovieFailure>>>(coroutineDispatcher) {

    override suspend fun execute(params: TimeWindow): DomainResult<List<Movie>, Failure<MovieFailure>> {
        return movieRepository.fetchTrendingMovies(timeWindow = params)
    }

}