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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun CloudVaultArt(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cloud_vault_motion")

    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_spin"
    )

    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vault_pulse"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

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
                            primaryColor.copy(alpha = 0.20f * breathingPulse),
                            secondaryColor.copy(alpha = 0.08f * breathingPulse),
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

            // Outer dashed orbit ring
            drawCircle(
                color = outlineVariant.copy(alpha = 0.45f),
                radius = baseRadius * 1.15f,
                center = centerOffset,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Orbiting data node
            rotate(degrees = orbitRotation, pivot = centerOffset) {
                val nodeOffset = Offset(
                    centerOffset.x + baseRadius * 1.15f,
                    centerOffset.y
                )
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = nodeOffset
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.3f),
                    radius = 9.dp.toPx(),
                    center = nodeOffset
                )
            }

            // Shield contour path
            val shieldWidth = baseRadius * 1.5f
            val shieldHeight = baseRadius * 1.8f
            val left = centerOffset.x - shieldWidth / 2f
            val right = centerOffset.x + shieldWidth / 2f
            val top = centerOffset.y - shieldHeight / 2f + 10.dp.toPx()
            val bottom = centerOffset.y + shieldHeight / 2f

            val shieldPath = Path().apply {
                moveTo(centerOffset.x, top)
                cubicTo(
                    right - 10.dp.toPx(), top,
                    right, top + 15.dp.toPx(),
                    right, centerOffset.y
                )
                cubicTo(
                    right, bottom - 25.dp.toPx(),
                    centerOffset.x + 15.dp.toPx(), bottom - 6.dp.toPx(),
                    centerOffset.x, bottom
                )
                cubicTo(
                    centerOffset.x - 15.dp.toPx(), bottom - 6.dp.toPx(),
                    left, bottom - 25.dp.toPx(),
                    left, centerOffset.y
                )
                cubicTo(
                    left, top + 15.dp.toPx(),
                    left + 10.dp.toPx(), top,
                    centerOffset.x, top
                )
                close()
            }

            // Shield background fill
            drawPath(
                path = shieldPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        surfaceVariant.copy(alpha = 0.7f),
                        surfaceVariant.copy(alpha = 0.35f)
                    )
                )
            )

            // Shield outline
            drawPath(
                path = shieldPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, secondaryColor)
                ),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        // Center lock & cloud icon badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.25f),
                            secondaryColor.copy(alpha = 0.25f)
                        )
                    )
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudDone,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
