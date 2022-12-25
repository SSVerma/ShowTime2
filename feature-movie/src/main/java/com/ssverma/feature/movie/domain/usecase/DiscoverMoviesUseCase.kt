package com.ssverma.feature.movie.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.repository.MovieRepository
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.usecase.UseCase
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