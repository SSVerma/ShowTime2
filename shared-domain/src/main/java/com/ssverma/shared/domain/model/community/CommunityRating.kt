package com.ssverma.shared.domain.model.community

import com.ssverma.shared.domain.model.MediaType
import kotlin.math.abs

data class CommunityRating(
    val mediaType: MediaType,
    val mediaId: Int,
    val ratingCount: Int = 0,
    val ratingSum: Double = 0.0,
    val averageRating: Float = 0f,
    val histogram: Map<Float, Int> = emptyMap(),
    val userRating: Float? = null,
    val sufficientThreshold: Int = MIN_SUFFICIENT_RATINGS_THRESHOLD
) {
    val maxBucketCount: Int
        get() = histogram.values.maxOrNull() ?: 0

    fun getBucketCount(ratingStep: Float): Int {
        return histogram[ratingStep] ?: 0
    }

    fun getBucketPercentage(ratingStep: Float): Float {
        if (ratingCount <= 0) return 0f
        val count = getBucketCount(ratingStep)
        return (count.toFloat() / ratingCount.toFloat()) * 100f
    }

    fun hasSufficientRatings(threshold: Int = sufficientThreshold): Boolean {
        return ratingCount >= threshold
    }

    companion object {
        const val MIN_SUFFICIENT_RATINGS_THRESHOLD = 5

        val VALID_RATING_STEPS: List<Float> = listOf(
            0.5f, 1.0f, 1.5f, 2.0f, 2.5f,
            3.0f, 3.5f, 4.0f, 4.5f, 5.0f
        )

        fun isValidRating(rating: Float): Boolean {
            return VALID_RATING_STEPS.any { step -> abs(step - rating) < 0.01f }
        }

        fun empty(mediaType: MediaType, mediaId: Int): CommunityRating {
            val emptyHistogram = VALID_RATING_STEPS.associateWith { 0 }
            return CommunityRating(
                mediaType = mediaType,
                mediaId = mediaId,
                ratingCount = 0,
                ratingSum = 0.0,
                averageRating = 0f,
                histogram = emptyHistogram,
                userRating = null
            )
        }
    }
}
