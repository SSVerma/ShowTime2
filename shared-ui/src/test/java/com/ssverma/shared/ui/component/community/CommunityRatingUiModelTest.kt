package com.ssverma.shared.ui.component.community

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityRatingUiModelTest {

    @Test
    fun `empty rating maps to cold-start ui model with minimum bar height`() {
        val uiModel = CommunityRating.empty(mediaType = MediaType.Movie, mediaId = 100).toUiModel()

        assertFalse(uiModel.hasAnyRatings)
        assertFalse(uiModel.hasSufficientRatings)
        assertEquals("0.0", uiModel.formattedAverageRating)
        assertNull(uiModel.formattedUserRating)
        assertEquals(10, uiModel.histogramBars.size)
        uiModel.histogramBars.forEach { bar ->
            assertEquals(0, bar.bucketCount)
            assertEquals(
                CommunityRatingUiModel.MIN_BAR_HEIGHT_FRACTION,
                bar.targetFraction,
                0.0001f
            )
            assertFalse(bar.isUserSelection)
        }
    }

    @Test
    fun `rating below threshold distinguishes gathering state from empty state`() {
        val uiModel = CommunityRating(
            mediaType = MediaType.Movie,
            mediaId = 100,
            ratingCount = 2,
            averageRating = 4.0f,
            userRating = 4.5f,
            sufficientThreshold = 5
        ).toUiModel()

        assertTrue(uiModel.hasAnyRatings)
        assertFalse(uiModel.hasSufficientRatings)
        assertEquals("4.5", uiModel.formattedUserRating)
    }

    @Test
    fun `active community rating computes accurate fractions, percentages, and user highlight`() {
        val uiModel = CommunityRating(
            mediaType = MediaType.Movie,
            mediaId = 100,
            ratingCount = 10,
            averageRating = 4.6f,
            histogram = mapOf(
                4.5f to 8,
                5.0f to 2
            ),
            userRating = 4.5f,
            sufficientThreshold = 5
        ).toUiModel()

        assertTrue(uiModel.hasSufficientRatings)
        assertEquals("4.6", uiModel.formattedAverageRating)

        val emptyBar = uiModel.histogramBars.first { it.step == 1.0f }
        assertEquals(
            CommunityRatingUiModel.MIN_BAR_HEIGHT_FRACTION,
            emptyBar.targetFraction,
            0.0001f
        )
        assertEquals(0, emptyBar.bucketPercentage)
        assertFalse(emptyBar.isUserSelection)

        val maxBar = uiModel.histogramBars.first { it.step == 4.5f }
        assertEquals(1.0f, maxBar.targetFraction, 0.0001f)
        assertEquals(80, maxBar.bucketPercentage)
        assertTrue(maxBar.isUserSelection)

        val fiveStarBar = uiModel.histogramBars.first { it.step == 5.0f }
        assertEquals(0.25f, fiveStarBar.targetFraction, 0.0001f)
        assertEquals(20, fiveStarBar.bucketPercentage)
        assertFalse(fiveStarBar.isUserSelection)
    }
}
