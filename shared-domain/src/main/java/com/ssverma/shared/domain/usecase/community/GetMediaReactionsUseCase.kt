package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMediaReactionsUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(mediaType: MediaType, mediaId: Int): Flow<MediaReactions> {
        return communityRepository.getMediaReactions(mediaType = mediaType, mediaId = mediaId)
    }
}
