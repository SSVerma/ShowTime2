package com.ssverma.feature.movie.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.MovieDiscoverConfig
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.UseCase
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.feature.movie.domain.repository.MovieRepository
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