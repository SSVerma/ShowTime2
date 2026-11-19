package com.ssverma.feature.community.data.datasource

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import kotlinx.coroutines.flow.Flow

interface RatingSandboxProvider {
    fun observeOverride(mediaType: MediaType, mediaId: Int): Flow<CommunityRating?>
    fun observeThreshold(): Flow<Int>
}
