package com.ssverma.shared.domain.usecase

import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.model.WatchProvider
import com.ssverma.shared.domain.repository.WatchProviderRepository
import javax.inject.Inject

class GetWatchProvidersUseCase @Inject constructor(
    private val watchProviderRepository: WatchProviderRepository
) {
    suspend operator fun invoke(mediaId: Int, isMovie: Boolean): CoreResult<WatchProvider?> {
        return if (isMovie) {
            watchProviderRepository.fetchMovieWatchProviders(movieId = mediaId)
        } else {
            watchProviderRepository.fetchTvShowWatchProviders(tvShowId = mediaId)
        }
    }
}
