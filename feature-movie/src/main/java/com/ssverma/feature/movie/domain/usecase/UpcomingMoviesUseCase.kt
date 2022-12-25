package com.ssverma.feature.movie.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.movie.domain.defaults.MovieDefaults
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.repository.MovieRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.usecase.NoParamUseCase
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