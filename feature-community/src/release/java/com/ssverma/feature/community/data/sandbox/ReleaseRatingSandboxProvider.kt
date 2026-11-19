package com.ssverma.feature.community.data.sandbox

import com.ssverma.feature.community.data.datasource.RatingSandboxProvider
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReleaseRatingSandboxProvider @Inject constructor() : RatingSandboxProvider {

    override fun observeOverride(mediaType: MediaType, mediaId: Int): Flow<CommunityRating?> {
        return flowOf(null)
    }

    override fun observeThreshold(): Flow<Int> {
        return flowOf(CommunityRating.MIN_SUFFICIENT_RATINGS_THRESHOLD)
    }
}
