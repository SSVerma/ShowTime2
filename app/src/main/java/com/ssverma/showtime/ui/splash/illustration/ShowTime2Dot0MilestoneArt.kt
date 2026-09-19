package com.ssverma.showtime.ui.splash.illustration

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.showtime.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance Compose Canvas illustration celebrating the ShowTime 2.0 release milestone.
 *
 * Features:
 * - Ambient cinematic stage beam and breathing radial nebula aura.
 * - Dual counter-rotating golden film-reel tracks with film sprockets.
 * - Orbiting cinema sparkle particles with dynamic luminosity.
 * - Central illuminated 2.0 emblem badge with neon outline glow.
 */
@Composable
fun ShowTime2Dot0MilestoneArt(
    badgeScale: Float,
    badgeAlpha: Float,
    shimmerSweep: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "showtime_2_dot_0_art_motion")

    val reelRotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reel_spin_fast"
    )

    val reelRotationSlow by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reel_spin_slow"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(210.dp)
    ) {
        // Atmospheric radial nebula aura
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.28f * auraPulse),
                            tertiaryColor.copy(alpha = 0.12f * auraPulse),
                            Color.Transparent
                        )
                    )
                )
        )

        // Vector Canvas Artwork
        Canvas(
            modifier = Modifier.size(190.dp)
        ) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.42f

            // Projector Spotlight Beam
            val beamPath = Path().apply {
                moveTo(centerOffset.x - 16.dp.toPx(), 0f)
                lineTo(centerOffset.x - baseRadius * 1.3f, size.height)
                lineTo(centerOffset.x + baseRadius * 1.3f, size.height)
                lineTo(centerOffset.x + 16.dp.toPx(), 0f)
                close()
            }
            drawPath(
                path = beamPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.18f * auraPulse),
                        tertiaryColor.copy(alpha = 0.05f * auraPulse),
                        Color.Transparent
                    )
                )
            )

            // Outer decorative orbital ring
            drawCircle(
                color = outlineVariant.copy(alpha = 0.4f * badgeAlpha),
                radius = baseRadius * 1.12f,
                center = centerOffset,
                style = Stroke(width = 1.dp.toPx())
            )

            // Outer Film Reel Rim (Sweep Gradient)
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(primaryColor, tertiaryColor, primaryColor),
                    center = centerOffset
                ),
                radius = baseRadius * 0.98f,
                center = centerOffset,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Outer rotating sprockets
            rotate(degrees = reelRotationFast, pivot = centerOffset) {
                val sprocketCount = 8
                for (i in 0 until sprocketCount) {
                    val angleRad = Math.toRadians(i * (360.0 / sprocketCount))
                    val sprocketX = centerOffset.x + (baseRadius * 0.98f * cos(angleRad)).toFloat()
                    val sprocketY = centerOffset.y + (baseRadius * 0.98f * sin(angleRad)).toFloat()

                    drawCircle(
                        color = primaryColor.copy(alpha = 0.85f * badgeAlpha),
                        radius = 2.5.dp.toPx(),
                        center = Offset(sprocketX, sprocketY)
                    )
                }
            }

            // Inner counter-rotating film reel track
            drawCircle(
                color = surfaceVariant.copy(alpha = 0.6f * badgeAlpha),
                radius = baseRadius * 0.74f,
                center = centerOffset,
                style = Stroke(width = 1.2.dp.toPx())
            )

            rotate(degrees = reelRotationSlow, pivot = centerOffset) {
                val spokeCount = 6
                for (i in 0 until spokeCount) {
                    val angleRad = Math.toRadians(i * (360.0 / spokeCount))
                    val spokeStartX =
                        centerOffset.x + (baseRadius * 0.42f * cos(angleRad)).toFloat()
                    val spokeStartY =
                        centerOffset.y + (baseRadius * 0.42f * sin(angleRad)).toFloat()
                    val spokeEndX = centerOffset.x + (baseRadius * 0.74f * cos(angleRad)).toFloat()
                    val spokeEndY = centerOffset.y + (baseRadius * 0.74f * sin(angleRad)).toFloat()

                    drawLine(
                        color = tertiaryColor.copy(alpha = 0.5f * badgeAlpha),
                        start = Offset(spokeStartX, spokeStartY),
                        end = Offset(spokeEndX, spokeEndY),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
            }

            // Orbiting cinema sparkle particles
            val sparkleAngles = listOf(25.0, 115.0, 205.0, 295.0)
            for ((index, baseAngle) in sparkleAngles.withIndex()) {
                val dynamicAngle =
                    Math.toRadians(baseAngle + (reelRotationFast * (if (index % 2 == 0) 0.6 else -0.4)))
                val distance = baseRadius * (1.18f + (0.08f * sin(auraPulse * (index + 1))))
                val sparkX = centerOffset.x + (distance * cos(dynamicAngle)).toFloat()
                val sparkY = centerOffset.y + (distance * sin(dynamicAngle)).toFloat()

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (index % 2 == 0) primaryColor else tertiaryColor,
                            Color.Transparent
                        ),
                        center = Offset(sparkX, sparkY),
                        radius = 6.dp.toPx()
                    ),
                    radius = 6.dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.9f * badgeAlpha),
                    radius = 1.5.dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )
            }
        }

        // Central Illuminated "2.0" Hero Emblem
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(84.dp)
                .scale(badgeScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor,
                            tertiaryColor
                        )
                    )
                )
        ) {
            // Shimmer Sweep Ring
            Canvas(modifier = Modifier.size(84.dp)) {
                if (shimmerSweep > 0f && shimmerSweep < 1f) {
                    val sweepAngle = 360f * shimmerSweep
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        ),
                        startAngle = sweepAngle - 40f,
                        sweepAngle = 80f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.version_2_0),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = onPrimaryColor,
                letterSpacing = (-1).sp
            )
        }
    }
}
