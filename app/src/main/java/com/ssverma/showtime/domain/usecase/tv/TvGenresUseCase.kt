package com.ssverma.showtime.domain.usecase.tv

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.model.Genre
import com.ssverma.core.domain.usecase.NoParamUseCase
import com.ssverma.showtime.domain.repository.TvShowRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TvGenresUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : NoParamUseCase<Result<List<Genre>, Failure.CoreFailure>>(coroutineDispatcher) {

    override suspend fun execute(): Result<List<Genre>, Failure.CoreFailure> {
        return tvShowRepository.fetchTvShowGenre()
    }
}