package com.ssverma.feature.movie.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.NoParamUseCase
import com.ssverma.feature.movie.domain.defaults.MovieDefaults
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.feature.movie.domain.repository.MovieRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class UpcomingMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : NoParamUseCase<Result<List<Movie>, Failure<MovieFailure>>>(coroutineDispatcher) {

    override suspend fun execute(): Result<List<Movie>, Failure<MovieFailure>> {
        val movieConfig = MovieDefaults.DiscoverDefaults.upcoming()
        return movieRepository.discoverMovies(movieConfig)
    }
}