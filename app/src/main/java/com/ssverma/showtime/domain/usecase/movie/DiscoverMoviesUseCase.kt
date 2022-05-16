package com.ssverma.showtime.domain.usecase.movie

import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.UseCase
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.showtime.domain.MovieDiscoverConfig
import com.ssverma.showtime.domain.failure.movie.MovieFailure
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.repository.MovieRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class DiscoverMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : UseCase<MovieDiscoverConfig, Result<List<Movie>, Failure<MovieFailure>>>(
    coroutineDispatcher
) {

    override suspend fun execute(params: MovieDiscoverConfig): Result<List<Movie>, Failure<MovieFailure>> {
        return movieRepository.discoverMovies(params)
    }
}