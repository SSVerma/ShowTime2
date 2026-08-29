package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.community.DeleteCommentParams
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(params: DeleteCommentParams): Result<Unit, Failure.CoreFailure> {
        return communityRepository.deleteComment(params)
    }
}
