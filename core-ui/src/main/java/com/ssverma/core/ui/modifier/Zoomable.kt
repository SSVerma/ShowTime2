package com.ssverma.core.ui.modifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.abs

fun Modifier.zoomable(
    minScale: Float = 1f,
    maxScale: Float = 3f,
    onScaleChanged: (Float) -> Unit = {}
): Modifier = composed {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val animatedScale by animateFloatAsState(targetValue = scale, label = "ScaleAnimation")
    val animatedOffset by animateOffsetAsState(targetValue = offset, label = "OffsetAnimation")

    var lastTapTime by remember { mutableLongStateOf(0L) }

    pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val currentTime = System.currentTimeMillis()

            // 1. Detect Double Tap
            if (currentTime - lastTapTime < 300L) {
                if (scale > 1.1f) {
                    scale = 1f
                    offset = Offset.Zero
                } else {
                    scale = maxScale
                }
                onScaleChanged(scale)
                down.consume()
            }
            lastTapTime = currentTime

            // 2. Transformation loop (Zoom & Pan)
            var zoom = 1f
            var pan = Offset.Zero
            var pastTouchSlop = false
            val touchSlop = viewConfiguration.touchSlop

            do {
                val event = awaitPointerEvent()
                val canceled = event.changes.any { it.isConsumed }
                if (!canceled) {
                    val zoomChange = event.calculateZoom()
                    val panChange = event.calculatePan()

                    if (!pastTouchSlop) {
                        zoom *= zoomChange
                        pan += panChange
                        val centroidSize = event.calculateCentroidSize(useCurrent = false)
                        val zoomMotion = abs(1 - zoom) * centroidSize
                        val panMotion = pan.getDistance()

                        if (zoomMotion > touchSlop || panMotion > touchSlop) {
                            pastTouchSlop = true
                        }
                    }

                    if (pastTouchSlop) {
                        if (zoomChange != 1f || panChange != Offset.Zero) {
                            val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
                            if (newScale != scale) {
                                scale = newScale
                                onScaleChanged(scale)
                            }

                            if (scale > 1f) {
                                offset += panChange
                            } else {
                                offset = Offset.Zero
                            }
                        }

                        // CONSUMPTION LOGIC:
                        // Only consume horizontal drags if zoomed in.
                        // Always consume if multi-touch (zoom gesture).
                        val isMultiTouch = event.changes.size > 1
                        if (scale > 1.05f || isMultiTouch) {
                            event.changes.forEach {
                                if (it.positionChanged()) {
                                    it.consume()
                                }
                            }
                        }
                    }
                }
            } while (!canceled && event.changes.any { it.pressed })
        }
    }
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
            translationX = animatedOffset.x
            translationY = animatedOffset.y
        }
}
