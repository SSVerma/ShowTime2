package com.ssverma.core.ui.layout

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveCarouselLayoutTest {

    @Test
    fun singleItem_takesFullAvailableContentWidth() {
        // Screen width 400dp, padding 16dp on each side -> content width = 368dp
        val width = calculateAdaptiveCarouselCardWidth(
            itemCount = 1,
            availableWidth = 400.dp,
            horizontalPadding = 16.dp,
            peekWidth = 48.dp,
            spacing = 12.dp
        )
        assertEquals(368.dp, width)
    }

    @Test
    fun zeroItems_returnsZeroOrEmptyContentWidth() {
        val width = calculateAdaptiveCarouselCardWidth(
            itemCount = 0,
            availableWidth = 400.dp,
            horizontalPadding = 16.dp
        )
        assertEquals(368.dp, width)
    }

    @Test
    fun multipleItems_sizesForPeekScroll() {
        // 400dp - 32dp padding = 368dp content width.
        // Target width = 368 - 48 (peek) - 12 (spacing) = 308dp.
        val width = calculateAdaptiveCarouselCardWidth(
            itemCount = 3,
            availableWidth = 400.dp,
            horizontalPadding = 16.dp,
            peekWidth = 48.dp,
            spacing = 12.dp,
            minMultiItemWidth = 220.dp,
            maxMultiItemWidth = 340.dp
        )
        assertEquals(308.dp, width)
    }

    @Test
    fun multipleItems_clampsToMinBound() {
        // Small screen 300dp, 32dp padding = 268dp content.
        // Target = 268 - 48 - 12 = 208dp, below min 220dp -> clamps to 220dp.
        val width = calculateAdaptiveCarouselCardWidth(
            itemCount = 2,
            availableWidth = 300.dp,
            horizontalPadding = 16.dp,
            peekWidth = 48.dp,
            spacing = 12.dp,
            minMultiItemWidth = 220.dp,
            maxMultiItemWidth = 340.dp
        )
        assertEquals(220.dp, width)
    }

    @Test
    fun multipleItems_clampsToMaxBound() {
        // Wide screen 600dp, 32dp padding = 568dp content.
        // Target = 568 - 48 - 12 = 508dp, above max 340dp -> clamps to 340dp.
        val width = calculateAdaptiveCarouselCardWidth(
            itemCount = 2,
            availableWidth = 600.dp,
            horizontalPadding = 16.dp,
            peekWidth = 48.dp,
            spacing = 12.dp,
            minMultiItemWidth = 220.dp,
            maxMultiItemWidth = 340.dp
        )
        assertEquals(340.dp, width)
    }
}
