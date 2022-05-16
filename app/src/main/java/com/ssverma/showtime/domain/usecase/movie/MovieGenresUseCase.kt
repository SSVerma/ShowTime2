package com.ssverma.showtime.domain.usecase.movie

import com.ssverma.core.domain.failure.Failure
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.repository.MovieRepository
import com.ssverma.core.domain.usecase.NoParamUseCase
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