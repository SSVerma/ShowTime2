package com.ssverma.feature.movie.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.model.Genre
import com.ssverma.core.domain.usecase.NoParamUseCase
import com.ssverma.feature.movie.domain.repository.MovieRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class MovieGenresUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : NoParamUseCase<Result<List<Genre>, Failure.CoreFailure>>(coroutineDispatcher) {

    override suspend fun execute(): Result<List<Genre>, Failure.CoreFailure> {
        return movieRepository.fetchMovieGenre()
    }
}