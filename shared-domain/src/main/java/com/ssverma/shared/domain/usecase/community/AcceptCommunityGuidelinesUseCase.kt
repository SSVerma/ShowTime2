package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class AcceptCommunityGuidelinesUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(accepted: Boolean = true) {
        communityRepository.setCommunityGuidelinesAccepted(accepted)
    }
}
