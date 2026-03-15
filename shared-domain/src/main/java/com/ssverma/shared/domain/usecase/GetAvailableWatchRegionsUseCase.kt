package com.ssverma.shared.domain.usecase

import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.domain.repository.WatchProviderRepository
import javax.inject.Inject

class GetAvailableWatchRegionsUseCase @Inject constructor(
    private val watchProviderRepository: WatchProviderRepository
) {
    suspend operator fun invoke(): CoreResult<List<WatchProviderRegion>> {
        return watchProviderRepository.fetchAvailableWatchRegions().asSuccess { regions ->
            regions.sortedBy { it.englishName }
        }
    }
}
