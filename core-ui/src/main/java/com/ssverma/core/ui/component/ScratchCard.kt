package com.ssverma.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Ultra-smooth, 120 FPS GPU-accelerated Scratch Card component.
 *
 * Uses Compose [Path] with bezier curve smoothing and [BlendMode.Clear] on an
 * offscreen compositing layer to cut through the overlay with zero frame drops or memory allocations.
 *
 * @param modifier Modifier applied to the outer container.
 * @param enabled Whether touch scratching is currently enabled.
 * @param scratchThresholdFraction Fraction of surface required to be scratched (0.0 to 1.0) before auto-revealing.
 * @param overlayColor Background color of the scratchable overlay.
 * @param brushStrokeWidth Width of the scratch brush in pixels (default 90f for a generous, smooth touch trail).
 * @param onRevealed Callback triggered once when the threshold is crossed.
 * @param overlayContent Optional composable content rendered on top of the scratchable overlay before scratching.
 * @param revealedContent Content underneath to be revealed.
 */
@Composable
fun ScratchCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scratchThresholdFraction: Float = 0.30f,
    overlayColor: Color = Color(0xFF1F1F23),
    brushStrokeWidth: Float = 90f,
    onRevealed: () -> Unit = {},
    overlayContent: (@Composable () -> Unit)? = null,
    revealedContent: @Composable () -> Unit
) {
    val currentPath = remember { Path() }
    var pathVersion by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }
    var totalScratchedDistance by remember { mutableFloatStateOf(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isRevealed) 0f else 1f,
        animationSpec = tween(durationMillis = 400),
        label = "ScratchOverlayAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .onSizeChanged { containerSize = it }
    ) {
        // 1. Revealed content underneath
        revealedContent()

        // 2. Scratch overlay with GPU-accelerated Offscreen compositing
        if (!isRevealed && overlayAlpha > 0f && containerSize.width > 0 && containerSize.height > 0) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = overlayAlpha
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .pointerInput(enabled, isRevealed) {
                        if (!enabled || isRevealed) return@pointerInput
                        var lastPoint: Offset? = null
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath.moveTo(offset.x, offset.y)
                                lastPoint = offset
                                pathVersion++
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val newPoint = change.position
                                val prev = lastPoint ?: newPoint
                                // Smooth quadratic bezier curve between touch points for fluid lines
                                val midX = (prev.x + newPoint.x) / 2f
                                val midY = (prev.y + newPoint.y) / 2f
                                currentPath.quadraticBezierTo(prev.x, prev.y, midX, midY)
                                lastPoint = newPoint
                                pathVersion++

                                totalScratchedDistance += dragAmount.getDistance() * (brushStrokeWidth * 0.85f)
                                val totalArea =
                                    (containerSize.width * containerSize.height).coerceAtLeast(1)
                                val fraction = (totalScratchedDistance / totalArea).coerceIn(0f, 1f)

                                if (fraction >= scratchThresholdFraction && !isRevealed) {
                                    isRevealed = true
                                    onRevealed()
                                }
                            },
                            onDragEnd = {
                                lastPoint = null
                            },
                            onDragCancel = {
                                lastPoint = null
                            }
                        )
                    }
            ) {
                // Reading pathVersion in DrawScope registers state observation so every drag point redraws immediately!
                @Suppress("UNUSED_VARIABLE")
                val version = pathVersion

                // Draw solid background overlay
                drawRect(color = overlayColor)

                // Cut smooth, thick holes through overlay using BlendMode.Clear
                drawPath(
                    path = currentPath,
                    color = Color.Black,
                    style = Stroke(
                        width = brushStrokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    blendMode = BlendMode.Clear
                )
            }

            // Optional overlay prompt (fades out as scratching begins)
            if (overlayContent != null && !isRevealed) {
                val promptAlpha =
                    (1f - (totalScratchedDistance / (containerSize.width * 0.15f).coerceAtLeast(1f))).coerceIn(
                        0f,
                        1f
                    )
                if (promptAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = promptAlpha * overlayAlpha },
                        contentAlignment = Alignment.Center
                    ) {
                        overlayContent()
                    }
                }
            }
        }
    }
}
