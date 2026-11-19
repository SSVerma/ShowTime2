package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class RemoveCommunityRatingUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(
        mediaType: MediaType,
        mediaId: Int
    ): Result<CommunityRating, Failure.CoreFailure> {
        return communityRepository.removeCommunityRating(
            mediaType = mediaType,
            mediaId = mediaId
        )
    }
}
