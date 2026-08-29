package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class PostCommentUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(params: PostCommentParams): Result<Comment, Failure.CoreFailure> {
        return communityRepository.postComment(params)
    }
}
