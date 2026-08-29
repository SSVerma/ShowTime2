package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.community.ToggleListUpvoteParams
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class ToggleCommunityListUpvoteUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(params: ToggleListUpvoteParams): Result<Unit, Failure.CoreFailure> {
        return communityRepository.toggleCommunityListUpvote(params = params)
    }
}
