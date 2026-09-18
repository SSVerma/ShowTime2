package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.repository.CommentQuotaManager
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

sealed interface PostCommentResult {
    data class Success(val comment: Comment) : PostCommentResult
    data object QuotaExceeded : PostCommentResult
    data class Error(val message: String? = null) : PostCommentResult
}

class PostCommentUseCase @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val commentQuotaManager: CommentQuotaManager
) {
    suspend operator fun invoke(
        params: PostCommentParams,
        isProActive: Boolean = false
    ): PostCommentResult {
        val canPost = commentQuotaManager.canPostComment(isProActive)
        if (!canPost) {
            return PostCommentResult.QuotaExceeded
        }

        val effectiveParams = if (params.isProUser != isProActive) {
            params.copy(isProUser = isProActive)
        } else {
            params
        }

        return when (val result = communityRepository.postComment(effectiveParams)) {
            is Result.Success -> {
                commentQuotaManager.recordCommentPosted()
                PostCommentResult.Success(result.data)
            }

            is Result.Error -> {
                PostCommentResult.Error(null)
            }
        }
    }
}

