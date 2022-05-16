package com.ssverma.showtime.domain.usecase.movie

import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.NoParamUseCase
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.showtime.domain.defaults.movie.MovieDefaults
import com.ssverma.showtime.domain.failure.movie.MovieFailure
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.repository.MovieRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class InCinemaMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : NoParamUseCase<Result<List<Movie>, Failure<MovieFailure>>>(coroutineDispatcher) {

    override suspend fun execute(): Result<List<Movie>, Failure<MovieFailure>> {
        val movieConfig = MovieDefaults.DiscoverDefaults.inCinemas()
        return movieRepository.discoverMovies(movieConfig)
    }
}