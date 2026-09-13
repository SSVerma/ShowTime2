package com.ssverma.showtime.ui.onboarding.illustration

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CinemaWelcomeArt(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cinema_welcome_motion")

    val reelRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reel_spin"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Atmospheric radial aura
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.22f * auraPulse),
                            secondaryColor.copy(alpha = 0.08f * auraPulse),
                            Color.Transparent
                        )
                    )
                )
        )

        Canvas(
            modifier = Modifier
                .size(190.dp)
                .fillMaxSize()
        ) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.38f

            // Projector beam path
            val beamPath = Path().apply {
                moveTo(centerOffset.x - 16.dp.toPx(), centerOffset.y - baseRadius * 1.15f)
                lineTo(centerOffset.x - baseRadius * 1.3f, size.height * 0.98f)
                lineTo(centerOffset.x + baseRadius * 1.3f, size.height * 0.98f)
                lineTo(centerOffset.x + 16.dp.toPx(), centerOffset.y - baseRadius * 1.15f)
                close()
            }
            drawPath(
                path = beamPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.12f * auraPulse),
                        Color.Transparent
                    )
                )
            )

            // Outer decorative track ring
            drawCircle(
                color = outlineColor.copy(alpha = 0.5f),
                radius = baseRadius * 1.08f,
                center = centerOffset,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Main film reel outer rim
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(primaryColor, secondaryColor, primaryColor),
                    center = centerOffset
                ),
                radius = baseRadius,
                center = centerOffset,
                style = Stroke(width = 4.dp.toPx())
            )

            // Inner reel groove
            drawCircle(
                color = surfaceVariant.copy(alpha = 0.7f),
                radius = baseRadius * 0.82f,
                center = centerOffset,
                style = Stroke(width = 2.dp.toPx())
            )

            // Rotating film reel sprockets and apertures
            rotate(degrees = reelRotation, pivot = centerOffset) {
                val spokeCount = 6
                for (i in 0 until spokeCount) {
                    val angleRad = Math.toRadians((i * (360.0 / spokeCount)))
                    val spokeStartX =
                        centerOffset.x + (baseRadius * 0.35f * cos(angleRad)).toFloat()
                    val spokeStartY =
                        centerOffset.y + (baseRadius * 0.35f * sin(angleRad)).toFloat()
                    val spokeEndX = centerOffset.x + (baseRadius * 0.80f * cos(angleRad)).toFloat()
                    val spokeEndY = centerOffset.y + (baseRadius * 0.80f * sin(angleRad)).toFloat()

                    drawLine(
                        color = primaryColor.copy(alpha = 0.6f),
                        start = Offset(spokeStartX, spokeStartY),
                        end = Offset(spokeEndX, spokeEndY),
                        strokeWidth = 2.5.dp.toPx()
                    )

                    // Reel holes
                    val holeCenterX =
                        centerOffset.x + (baseRadius * 0.58f * cos(angleRad)).toFloat()
                    val holeCenterY =
                        centerOffset.y + (baseRadius * 0.58f * sin(angleRad)).toFloat()
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.25f),
                        radius = 8.dp.toPx(),
                        center = Offset(holeCenterX, holeCenterY)
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.85f),
                        radius = 8.dp.toPx(),
                        center = Offset(holeCenterX, holeCenterY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // Central reel hub
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.8f)),
                    center = centerOffset
                ),
                radius = baseRadius * 0.28f,
                center = centerOffset
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = baseRadius * 0.10f,
                center = centerOffset
            )
        }
    }
}
