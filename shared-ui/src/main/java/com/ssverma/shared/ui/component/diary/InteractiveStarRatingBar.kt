package com.ssverma.shared.ui.component.diary

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.R
import kotlin.math.round

@Composable
fun InteractiveStarRatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp = 38.dp,
    starSpacing: Dp = 6.dp
) {
    var barWidthPx by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    val currentOnRatingChanged by rememberUpdatedState(onRatingChanged)
    val currentRating by rememberUpdatedState(rating)

    fun calculateRating(touchX: Float): Float {
        if (barWidthPx <= 0f) return currentRating
        val clampedX = touchX.coerceIn(0f, barWidthPx)
        val fraction = clampedX / barWidthPx
        val rawValue = fraction * maxStars.toFloat()
        return (round(rawValue * 2f) / 2f).coerceIn(0.5f, maxStars.toFloat())
    }

    fun handleTouch(touchX: Float) {
        val newRating = calculateRating(touchX)
        if (newRating != currentRating) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentOnRatingChanged(newRating)
        }
    }

    val cd = stringResource(R.string.diary_dialog_star_cd, rating.toInt())
    val stateDesc = stringResource(R.string.diary_dialog_rating_format, rating)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(starSpacing),
        modifier = modifier
            .onSizeChanged { barWidthPx = it.width.toFloat() }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    handleTouch(down.position.x)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) break
                        change.consume()
                        handleTouch(change.position.x)
                    }
                }
            }
            .semantics(mergeDescendants = true) {
                contentDescription = cd
                stateDescription = stateDesc
            }
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        for (i in 1..maxStars) {
            val starValue = i.toFloat()
            val isFull = rating >= starValue
            val isHalf = !isFull && rating >= starValue - 0.5f

            val icon = when {
                isFull -> Icons.Rounded.Star
                isHalf -> Icons.AutoMirrored.Rounded.StarHalf
                else -> Icons.Rounded.StarBorder
            }

            val starScale by animateFloatAsState(
                targetValue = if (isFull || isHalf) 1.12f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "star_scale_$i"
            )

            val tint = if (isFull || isHalf) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(2.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .size(starSize)
                        .graphicsLayer {
                            scaleX = starScale
                            scaleY = starScale
                        }
                )
            }
        }
    }
}
