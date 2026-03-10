package com.ssverma.shared.domain.usecase

import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.domain.repository.WatchProviderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvailableWatchRegionsUseCase @Inject constructor(
    private val watchProviderRepository: WatchProviderRepository
) {
    suspend operator fun invoke(): CoreResult<List<WatchProviderRegion>> {
        return watchProviderRepository.fetchAvailableWatchRegions()
    }
}
