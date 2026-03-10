package com.ssverma.feature.tv.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.WatchProvider
import com.ssverma.shared.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class TvShowWatchProvidersUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val tvShowRepository: TvShowRepository
) : UseCase<Int, Result<Map<String, WatchProvider>, Failure<TvShowFailure>>>(coroutineDispatcher) {

    override suspend fun execute(params: Int): Result<Map<String, WatchProvider>, Failure<TvShowFailure>> {
        return tvShowRepository.fetchWatchProviders(params)
    }
}
