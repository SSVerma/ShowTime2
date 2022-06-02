package com.ssverma.feature.movie.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.UseCase
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.feature.movie.domain.model.MovieDetailsConfig
import com.ssverma.feature.movie.domain.repository.MovieRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class MovieDetailsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : UseCase<MovieDetailsConfig, Result<Movie, Failure<MovieFailure>>>(coroutineDispatcher) {

    override suspend fun execute(params: MovieDetailsConfig): Result<Movie, Failure<MovieFailure>> {
        return movieRepository.fetchMovieDetails(
            movieDetailsConfig = params
        )
    }
}