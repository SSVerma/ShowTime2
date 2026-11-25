package com.ssverma.core.ui.modifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val HoverAnimationDurationMs = 140
private const val WheelScrollPixelsMultiplier = 72f

/**
 * Adds desktop-grade mouse hover feedback (hand cursor and subtle draw-phase scale) without
 * intercepting clicks or triggering composition-phase recompositions.
 */
@Composable
fun Modifier.desktopCardHover(
    hoverScale: Float = 1.02f
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) hoverScale else 1f,
        animationSpec = tween(durationMillis = HoverAnimationDurationMs),
        label = "DesktopCardHoverScale"
    )

    return this
        .pointerHoverIcon(PointerIcon.Hand)
        .hoverable(interactionSource = interactionSource)
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        }
}

/**
 * Captures mouse secondary-button (right-click) gestures in [PointerEventPass.Initial] so
 * context menus open cleanly without triggering the underlying card's primary click handler.
 */
fun Modifier.desktopSecondaryClick(
    onSecondaryClick: () -> Unit
): Modifier = pointerInput(onSecondaryClick) {
    awaitEachGesture {
        var sawSecondaryPress = false
        while (true) {
            val pointerEvent = awaitPointerEvent(pass = PointerEventPass.Initial)
            if (pointerEvent.type == PointerEventType.Press && pointerEvent.buttons.isSecondaryPressed) {
                sawSecondaryPress = true
                pointerEvent.changes.forEach { pointerChange -> pointerChange.consume() }
            } else if (sawSecondaryPress && pointerEvent.type == PointerEventType.Release) {
                pointerEvent.changes.forEach { pointerChange -> pointerChange.consume() }
                onSecondaryClick()
                break
            } else if (pointerEvent.changes.all { pointerChange -> !pointerChange.pressed }) {
                break
            }
        }
    }
}

/**
 * Translates vertical or horizontal mouse-wheel scroll deltas into smooth horizontal scrolling
 * on desktop and Chromebook trackpads/mice when hovering over horizontal carousels.
 */
fun Modifier.horizontalWheelScroll(
    scrollableState: ScrollableState,
    coroutineScope: CoroutineScope
): Modifier = pointerInput(scrollableState, coroutineScope) {
    awaitPointerEventScope {
        while (true) {
            val pointerEvent = awaitPointerEvent(pass = PointerEventPass.Main)
            if (pointerEvent.type == PointerEventType.Scroll) {
                val primaryChange = pointerEvent.changes.firstOrNull() ?: continue
                val scrollDelta = primaryChange.scrollDelta
                val primaryDelta = if (abs(scrollDelta.x) > abs(scrollDelta.y)) {
                    scrollDelta.x
                } else {
                    scrollDelta.y
                }
                if (primaryDelta != 0f) {
                    val canScrollInDirection = if (primaryDelta > 0f) {
                        scrollableState.canScrollForward
                    } else {
                        scrollableState.canScrollBackward
                    }
                    if (canScrollInDirection) {
                        primaryChange.consume()
                        coroutineScope.launch {
                            scrollableState.scrollBy(primaryDelta * WheelScrollPixelsMultiplier)
                        }
                    }
                }
            }
        }
    }
}
