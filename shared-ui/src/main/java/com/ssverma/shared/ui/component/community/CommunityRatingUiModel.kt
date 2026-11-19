package com.ssverma.shared.ui.component.community

import androidx.compose.runtime.Immutable
import com.ssverma.shared.domain.model.community.CommunityRating
import java.util.Locale
import kotlin.math.abs

@Immutable
data class RatingHistogramBarUiModel(
    val step: Float,
    val bucketCount: Int,
    val bucketPercentage: Int,
    val targetFraction: Float,
    val isUserSelection: Boolean
)

@Immutable
data class CommunityRatingUiModel(
    val ratingCount: Int = 0,
    val averageRating: Float = 0f,
    val formattedAverageRating: String = "0.0",
    val userRating: Float? = null,
    val formattedUserRating: String? = null,
    val hasSufficientRatings: Boolean = false,
    val hasAnyRatings: Boolean = false,
    val sufficientThreshold: Int = CommunityRating.MIN_SUFFICIENT_RATINGS_THRESHOLD,
    val histogramBars: List<RatingHistogramBarUiModel> = emptyHistogramBars()
) {
    companion object {
        const val MIN_BAR_HEIGHT_FRACTION = 0.06f

        fun emptyHistogramBars(): List<RatingHistogramBarUiModel> {
            return CommunityRating.VALID_RATING_STEPS.map { step ->
                RatingHistogramBarUiModel(
                    step = step,
                    bucketCount = 0,
                    bucketPercentage = 0,
                    targetFraction = MIN_BAR_HEIGHT_FRACTION,
                    isUserSelection = false
                )
            }
        }
    }
}

fun CommunityRating.toUiModel(): CommunityRatingUiModel {
    val maxBucket = maxBucketCount
    val currentUserRating = userRating
    val bars = CommunityRating.VALID_RATING_STEPS.map { step ->
        val bucketCount = getBucketCount(ratingStep = step)
        val targetFraction = if (maxBucket > 0) {
            (bucketCount.toFloat() / maxBucket.toFloat())
                .coerceIn(CommunityRatingUiModel.MIN_BAR_HEIGHT_FRACTION, 1f)
        } else {
            CommunityRatingUiModel.MIN_BAR_HEIGHT_FRACTION
        }
        val isUserSelection = currentUserRating != null && abs(currentUserRating - step) < 0.01f

        RatingHistogramBarUiModel(
            step = step,
            bucketCount = bucketCount,
            bucketPercentage = getBucketPercentage(ratingStep = step).toInt(),
            targetFraction = targetFraction,
            isUserSelection = isUserSelection
        )
    }

    return CommunityRatingUiModel(
        ratingCount = ratingCount,
        averageRating = averageRating,
        formattedAverageRating = String.format(Locale.US, "%.1f", averageRating),
        userRating = userRating,
        formattedUserRating = userRating?.let { String.format(Locale.US, "%.1f", it) },
        hasSufficientRatings = hasSufficientRatings(),
        hasAnyRatings = ratingCount > 0,
        sufficientThreshold = sufficientThreshold,
        histogramBars = bars
    )
}
