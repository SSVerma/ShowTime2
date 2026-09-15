package com.ssverma.feature.movie.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.repository.MovieRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.movie.MovieCollection
import com.ssverma.shared.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class MovieCollectionUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : UseCase<Int, Result<MovieCollection, Failure<MovieFailure>>>(coroutineDispatcher) {

    override suspend fun execute(params: Int): Result<MovieCollection, Failure<MovieFailure>> {
        return movieRepository.fetchMovieCollection(
            collectionId = params
        )
    }
}
