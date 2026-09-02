package com.ssverma.shared.domain.usecase.discovery

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import javax.inject.Inject

class GetRouletteSurpriseUseCase @Inject constructor(
    private val getUniversalDiscoveryUseCase: GetUniversalDiscoveryUseCase
) {
    suspend operator fun invoke(
        filter: UniversalDiscoveryFilter
    ): Result<UniversalMediaItem, Failure.CoreFailure> {
        val surpriseFilter = filter.copy(
            minRating = maxOf(filter.minRating ?: 7.0f, 7.2f)
        )

        val result = getUniversalDiscoveryUseCase(surpriseFilter, page = 1)

        return when (result) {
            is Result.Success -> {
                val items = result.data
                if (items.isNotEmpty()) {
                    Result.Success(items.random())
                } else {
                    Result.Error(Failure.CoreFailure.UnexpectedFailure)
                }
            }

            is Result.Error -> Result.Error(result.error)
        }
    }
}
