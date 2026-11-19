package com.ssverma.feature.community.data.datasource

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import java.util.Locale
import kotlin.math.abs

internal data class AggregateRatingSnapshot(
    val ratingCount: Int = 0,
    val ratingSum: Double = 0.0,
    val averageRating: Float = 0f,
    val histogram: Map<Float, Int> = emptyHistogram()
) {
    companion object {
        fun emptyHistogram(): Map<Float, Int> {
            return CommunityRating.VALID_RATING_STEPS.associateWith { 0 }
        }
    }
}

internal sealed interface OptimisticRatingAction {
    data object None : OptimisticRatingAction
    data class Submitted(val rating: Float) : OptimisticRatingAction
    data object Removed : OptimisticRatingAction
}

internal object CommunityRatingAggregator {

    fun ratingToKey(rating: Float): String {
        return String.format(Locale.US, "%.1f", rating)
    }

    fun parseHistogram(rawHistogram: Map<*, *>?): Map<Float, Int> {
        return CommunityRating.VALID_RATING_STEPS.associateWith { step ->
            val bucketKey = ratingToKey(rating = step)
            ((rawHistogram?.get(bucketKey) as? Number)?.toInt() ?: 0).coerceAtLeast(0)
        }
    }

    fun toFirestoreHistogram(histogram: Map<Float, Int>): Map<String, Int> {
        return CommunityRating.VALID_RATING_STEPS.associate { step ->
            ratingToKey(rating = step) to (histogram[step] ?: 0).coerceAtLeast(0)
        }
    }

    fun applySubmit(
        current: AggregateRatingSnapshot,
        previousRating: Float?,
        newRating: Float
    ): AggregateRatingSnapshot {
        val updatedHistogram = CommunityRating.VALID_RATING_STEPS
            .associateWith { step -> (current.histogram[step] ?: 0).coerceAtLeast(0) }
            .toMutableMap()

        var updatedCount = current.ratingCount.coerceAtLeast(0)
        var updatedSum = current.ratingSum.coerceAtLeast(0.0)

        val validPrevious = previousRating?.takeIf { CommunityRating.isValidRating(it) }
        if (validPrevious != null) {
            val prevBucketCount = updatedHistogram[validPrevious] ?: 0
            updatedHistogram[validPrevious] = (prevBucketCount - 1).coerceAtLeast(0)
            updatedSum = (updatedSum - validPrevious.toDouble()).coerceAtLeast(0.0)
        } else {
            updatedCount += 1
        }

        val targetBucketCount = updatedHistogram[newRating] ?: 0
        updatedHistogram[newRating] = targetBucketCount + 1
        updatedSum += newRating.toDouble()

        val updatedAverage = if (updatedCount > 0) (updatedSum / updatedCount).toFloat() else 0f

        return AggregateRatingSnapshot(
            ratingCount = updatedCount,
            ratingSum = updatedSum,
            averageRating = updatedAverage,
            histogram = updatedHistogram
        )
    }

    fun applyRemove(
        current: AggregateRatingSnapshot,
        previousRating: Float
    ): AggregateRatingSnapshot {
        val updatedHistogram = CommunityRating.VALID_RATING_STEPS
            .associateWith { step -> (current.histogram[step] ?: 0).coerceAtLeast(0) }
            .toMutableMap()

        val updatedCount = (current.ratingCount - 1).coerceAtLeast(0)
        val updatedSum = if (updatedCount == 0) {
            0.0
        } else {
            (current.ratingSum - previousRating.toDouble()).coerceAtLeast(0.0)
        }

        if (CommunityRating.isValidRating(previousRating)) {
            val prevBucketCount = updatedHistogram[previousRating] ?: 0
            updatedHistogram[previousRating] = (prevBucketCount - 1).coerceAtLeast(0)
        }

        val updatedAverage = if (updatedCount > 0) (updatedSum / updatedCount).toFloat() else 0f

        return AggregateRatingSnapshot(
            ratingCount = updatedCount,
            ratingSum = updatedSum,
            averageRating = updatedAverage,
            histogram = updatedHistogram
        )
    }

    fun resolveDisplayState(
        mediaType: MediaType,
        mediaId: Int,
        remoteAggregate: AggregateRatingSnapshot,
        localUserRating: Float?,
        remoteUserRating: Float?,
        optimisticAction: OptimisticRatingAction,
        sufficientThreshold: Int
    ): CommunityRating {
        val (resolvedAggregate, resolvedUserRating) = when (optimisticAction) {
            OptimisticRatingAction.Removed -> {
                val adjustedAggregate = if (remoteUserRating != null) {
                    applyRemove(
                        current = remoteAggregate,
                        previousRating = remoteUserRating
                    )
                } else {
                    remoteAggregate
                }
                adjustedAggregate to null
            }

            is OptimisticRatingAction.Submitted -> {
                val remoteMatches = remoteUserRating != null &&
                        abs(remoteUserRating - optimisticAction.rating) < 0.01f
                val adjustedAggregate = if (remoteMatches) {
                    remoteAggregate
                } else {
                    applySubmit(
                        current = remoteAggregate,
                        previousRating = remoteUserRating,
                        newRating = optimisticAction.rating
                    )
                }
                adjustedAggregate to optimisticAction.rating
            }

            OptimisticRatingAction.None -> {
                remoteAggregate to (localUserRating ?: remoteUserRating)
            }
        }

        return CommunityRating(
            mediaType = mediaType,
            mediaId = mediaId,
            ratingCount = resolvedAggregate.ratingCount,
            ratingSum = resolvedAggregate.ratingSum,
            averageRating = resolvedAggregate.averageRating,
            histogram = resolvedAggregate.histogram,
            userRating = resolvedUserRating,
            sufficientThreshold = sufficientThreshold
        )
    }
}
