package com.ssverma.core.ui.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Pure calculation helper for adaptive carousel card width.
 * - When [itemCount] <= 1: Takes the full available content width ([availableWidth] minus horizontal paddings),
 *   preventing single cards from leaving large empty gaps on the right edge.
 * - When [itemCount] > 1: Sizes cards so that the next card's partial width ("peek")
 *   is visible on the right edge, naturally signaling that the section is horizontally scrollable.
 */
fun calculateAdaptiveCarouselCardWidth(
    itemCount: Int,
    availableWidth: Dp,
    horizontalPadding: Dp = 16.dp,
    peekWidth: Dp = 48.dp,
    spacing: Dp = 12.dp,
    minMultiItemWidth: Dp = 220.dp,
    maxMultiItemWidth: Dp = 340.dp
): Dp {
    val contentWidth = (availableWidth - (horizontalPadding * 2)).coerceAtLeast(0.dp)
    if (itemCount <= 1) {
        return contentWidth
    }

    val targetWidth = contentWidth - peekWidth - spacing
    return targetWidth.coerceIn(minMultiItemWidth, maxMultiItemWidth)
}

/**
 * Calculates adaptive card width for horizontal carousels.
 */
@Composable
fun rememberAdaptiveCarouselCardWidth(
    itemCount: Int,
    containerWidth: Dp,
    horizontalPadding: Dp = 16.dp,
    peekWidth: Dp = 48.dp,
    spacing: Dp = 12.dp,
    minMultiItemWidth: Dp = 220.dp,
    maxMultiItemWidth: Dp = 340.dp
): Dp {
    val available = if (containerWidth > 0.dp && containerWidth != Dp.Infinity) {
        containerWidth
    } else {
        LocalConfiguration.current.screenWidthDp.dp
    }

    return calculateAdaptiveCarouselCardWidth(
        itemCount = itemCount,
        availableWidth = available,
        horizontalPadding = horizontalPadding,
        peekWidth = peekWidth,
        spacing = spacing,
        minMultiItemWidth = minMultiItemWidth,
        maxMultiItemWidth = maxMultiItemWidth
    )
}

/**
 * A container for horizontal carousels that provides a dynamically calculated [cardWidth]
 * based on container width and [itemCount].
 */
@Composable
fun AdaptiveHorizontalCarousel(
    itemCount: Int,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    peekWidth: Dp = 48.dp,
    spacing: Dp = 12.dp,
    minMultiItemWidth: Dp = 220.dp,
    maxMultiItemWidth: Dp = 340.dp,
    content: @Composable (cardWidth: Dp) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cardWidth = rememberAdaptiveCarouselCardWidth(
            itemCount = itemCount,
            containerWidth = maxWidth,
            horizontalPadding = horizontalPadding,
            peekWidth = peekWidth,
            spacing = spacing,
            minMultiItemWidth = minMultiItemWidth,
            maxMultiItemWidth = maxMultiItemWidth
        )
        content(cardWidth)
    }
}
