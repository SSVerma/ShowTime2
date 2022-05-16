package com.ssverma.showtime.domain.usecase.movie

import com.ssverma.core.domain.failure.Failure
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.showtime.domain.failure.movie.MovieFailure
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.model.movie.MovieDetailsConfig
import com.ssverma.showtime.domain.repository.MovieRepository
import com.ssverma.core.domain.usecase.UseCase
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