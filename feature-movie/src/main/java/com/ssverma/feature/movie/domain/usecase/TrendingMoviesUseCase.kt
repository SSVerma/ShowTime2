package com.ssverma.feature.movie.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.usecase.UseCase
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.feature.movie.domain.repository.MovieRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TrendingMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : UseCase<TimeWindow, Result<List<Movie>, Failure<MovieFailure>>>(coroutineDispatcher) {

    override suspend fun execute(params: TimeWindow): Result<List<Movie>, Failure<MovieFailure>> {
        return movieRepository.fetchTrendingMovies(timeWindow = params)
    }

}