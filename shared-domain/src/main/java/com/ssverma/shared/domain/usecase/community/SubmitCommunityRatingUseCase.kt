package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class SubmitCommunityRatingUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(
        mediaType: MediaType,
        mediaId: Int,
        rating: Float
    ): Result<CommunityRating, Failure.CoreFailure> {
        if (!CommunityRating.isValidRating(rating)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        return communityRepository.submitCommunityRating(
            mediaType = mediaType,
            mediaId = mediaId,
            rating = rating
        )
    }
}
