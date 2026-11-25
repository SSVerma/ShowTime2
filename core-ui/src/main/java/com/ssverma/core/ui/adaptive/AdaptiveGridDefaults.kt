package com.ssverma.core.ui.adaptive

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standardized adaptive grid sizing and readable content width defaults across all form factors.
 */
object AdaptiveGridDefaults {
    val PosterMinCellWidth: Dp = 152.dp
    val CompactPosterMinCellWidth: Dp = 112.dp
    val BackdropMinCellWidth: Dp = 240.dp
    val CommunityCardMinCellWidth: Dp = 310.dp
    val ReadableContentMaxWidth: Dp = 840.dp
    val OnboardingContentMaxWidth: Dp = 680.dp
    val ModalCardMaxWidth: Dp = 560.dp

    val PosterGridCells: GridCells = GridCells.Adaptive(minSize = PosterMinCellWidth)
    val CompactPosterGridCells: GridCells = GridCells.Adaptive(minSize = CompactPosterMinCellWidth)
    val BackdropGridCells: GridCells = GridCells.Adaptive(minSize = BackdropMinCellWidth)
    val CommunityCardGridCells: GridCells = GridCells.Adaptive(minSize = CommunityCardMinCellWidth)
}

/**
 * Constrains long-form forms, onboarding steps, and settings containers to an ergonomic width
 * on wide tablet and desktop monitors while remaining full-width on compact screens.
 */
fun Modifier.adaptiveReadableWidth(
    maxWidth: Dp = AdaptiveGridDefaults.ReadableContentMaxWidth
): Modifier = this.widthIn(max = maxWidth)
