package com.ssverma.feature.community.data.sandbox

import com.ssverma.feature.community.data.datasource.RatingSandboxProvider
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import com.ssverma.shared.domain.model.community.DebugRatingOverride
import com.ssverma.shared.domain.model.community.RatingOverrideProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugRatingSandboxProvider @Inject constructor(
    private val ratingOverrideProvider: RatingOverrideProvider
) : RatingSandboxProvider {

    override fun observeOverride(mediaType: MediaType, mediaId: Int): Flow<CommunityRating?> {
        return combine(
            ratingOverrideProvider.ratingOverride,
            ratingOverrideProvider.ratingThreshold
        ) { override, threshold ->
            when (override) {
                DebugRatingOverride.FORCE_COLD_START -> {
                    CommunityRating.empty(mediaType = mediaType, mediaId = mediaId).copy(
                        sufficientThreshold = threshold
                    )
                }

                DebugRatingOverride.FORCE_BELOW_THRESHOLD -> {
                    CommunityRating(
                        mediaType = mediaType,
                        mediaId = mediaId,
                        ratingCount = 3,
                        ratingSum = 11.5,
                        averageRating = 3.8f,
                        histogram = mapOf(
                            3.5f to 1,
                            4.0f to 2
                        ),
                        userRating = null,
                        sufficientThreshold = threshold
                    )
                }

                DebugRatingOverride.FORCE_ACTIVE_HIGH -> {
                    CommunityRating(
                        mediaType = mediaType,
                        mediaId = mediaId,
                        ratingCount = 42,
                        ratingSum = 184.8,
                        averageRating = 4.4f,
                        histogram = mapOf(
                            3.0f to 2,
                            3.5f to 6,
                            4.0f to 14,
                            4.5f to 15,
                            5.0f to 5
                        ),
                        userRating = null,
                        sufficientThreshold = threshold
                    )
                }

                DebugRatingOverride.FORCE_USER_RATED -> {
                    CommunityRating(
                        mediaType = mediaType,
                        mediaId = mediaId,
                        ratingCount = 18,
                        ratingSum = 73.8,
                        averageRating = 4.1f,
                        histogram = mapOf(
                            3.5f to 4,
                            4.0f to 6,
                            4.5f to 6,
                            5.0f to 2
                        ),
                        userRating = 4.5f,
                        sufficientThreshold = threshold
                    )
                }

                DebugRatingOverride.AUTO -> null
            }
        }
    }

    override fun observeThreshold(): Flow<Int> = ratingOverrideProvider.ratingThreshold
}
