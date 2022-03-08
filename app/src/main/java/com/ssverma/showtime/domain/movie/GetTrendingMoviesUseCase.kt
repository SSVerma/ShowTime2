package com.ssverma.showtime.domain.movie

import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.core.Failure
import com.ssverma.showtime.domain.core.UseCase
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.domain.repository.MovieRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetTrendingMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : UseCase<TimeWindow, List<Movie>, Failure<MovieFailure>>(coroutineDispatcher) {

    override suspend fun execute(params: TimeWindow): DomainResult<List<Movie>, Failure<MovieFailure>> {
        return movieRepository.fetchTrendingMovies(timeWindow = params)
    }

}