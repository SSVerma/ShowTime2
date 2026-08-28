package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class ToggleMediaReactionUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(
        mediaType: MediaType,
        mediaId: Int,
        tag: MediaReactionTag
    ): Result<MediaReactions, Failure.CoreFailure> {
        return communityRepository.toggleMediaReaction(
            mediaType = mediaType,
            mediaId = mediaId,
            tag = tag
        )
    }
}
