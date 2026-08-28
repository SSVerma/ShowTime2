package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getMediaReactions(mediaType: MediaType, mediaId: Int): Flow<MediaReactions>
    suspend fun toggleMediaReaction(
        mediaType: MediaType,
        mediaId: Int,
        tag: MediaReactionTag
    ): Result<MediaReactions, Failure.CoreFailure>
}
