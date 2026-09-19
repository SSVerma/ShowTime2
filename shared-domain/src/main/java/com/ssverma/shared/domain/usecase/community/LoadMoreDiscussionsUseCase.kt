package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class LoadMoreDiscussionsUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(
        target: DiscussionTarget,
        lastCommentEpochMs: Long
    ): Result<List<Comment>, Failure.CoreFailure> {
        return communityRepository.loadMoreDiscussions(
            target = target,
            lastCommentEpochMs = lastCommentEpochMs
        )
    }
}
