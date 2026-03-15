package com.ssverma.feature.tv.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.usecase.NoParamUseCase
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvGenresUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : NoParamUseCase<Result<List<Genre>, Failure.CoreFailure>>(coroutineDispatcher) {

    private var cachedGenres: List<Genre>? = null

    override suspend fun execute(): Result<List<Genre>, Failure.CoreFailure> {
        cachedGenres?.let { return Result.Success(it) }

        val result = tvShowRepository.fetchTvShowGenre()
        if (result is Result.Success) {
            cachedGenres = result.data
        }
        return result
    }
}