package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.community.ToggleCommentUpvoteParams
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class ToggleCommentUpvoteUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(params: ToggleCommentUpvoteParams): Result<Unit, Failure.CoreFailure> {
        return communityRepository.toggleCommentUpvote(params)
    }
}
