package com.ssverma.shared.domain.model.community

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.model.MediaType
import org.junit.Test

class CommunityRatingTest {

    @Test
    fun `empty rating initializes with zero counts across all valid steps`() {
        val emptyRating = CommunityRating.empty(mediaType = MediaType.Movie, mediaId = 550)

        assertThat(emptyRating.mediaType).isEqualTo(MediaType.Movie)
        assertThat(emptyRating.mediaId).isEqualTo(550)
        assertThat(emptyRating.ratingCount).isEqualTo(0)
        assertThat(emptyRating.ratingSum).isEqualTo(0.0)
        assertThat(emptyRating.averageRating).isEqualTo(0f)
        assertThat(emptyRating.userRating).isNull()
        assertThat(emptyRating.histogram).hasSize(10)
        assertThat(emptyRating.hasSufficientRatings()).isFalse()

        CommunityRating.VALID_RATING_STEPS.forEach { step ->
            assertThat(emptyRating.getBucketCount(step)).isEqualTo(0)
            assertThat(emptyRating.getBucketPercentage(step)).isEqualTo(0f)
        }
    }

    @Test
    fun `isValidRating correctly validates ratings on 0_5 to 5_0 half-step scale`() {
        val validSteps = listOf(0.5f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f)
        validSteps.forEach { step ->
            assertThat(CommunityRating.isValidRating(step)).isTrue()
        }

        val invalidSteps = listOf(0f, 0.25f, 1.2f, 2.75f, 4.2f, 5.5f, -1f, 10f)
        invalidSteps.forEach { step ->
            assertThat(CommunityRating.isValidRating(step)).isFalse()
        }
    }

    @Test
    fun `getBucketPercentage and maxBucketCount compute accurate statistics`() {
        val rating = CommunityRating(
            mediaType = MediaType.Movie,
            mediaId = 550,
            ratingCount = 100,
            ratingSum = 400.0,
            averageRating = 4.0f,
            histogram = mapOf(
                3.5f to 25,
                4.0f to 50,
                4.5f to 25
            )
        )

        assertThat(rating.maxBucketCount).isEqualTo(50)
        assertThat(rating.getBucketPercentage(4.0f)).isEqualTo(50f)
        assertThat(rating.getBucketPercentage(3.5f)).isEqualTo(25f)
        assertThat(rating.getBucketPercentage(1.0f)).isEqualTo(0f)
        assertThat(rating.hasSufficientRatings(threshold = 5)).isTrue()
    }
}
