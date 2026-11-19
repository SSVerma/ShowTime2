package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommunityRatingUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(mediaType: MediaType, mediaId: Int): Flow<CommunityRating> {
        return communityRepository.getCommunityRating(mediaType = mediaType, mediaId = mediaId)
    }
}
