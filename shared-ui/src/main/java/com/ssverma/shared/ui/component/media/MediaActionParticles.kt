package com.ssverma.shared.ui.component.media

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

enum class MediaActionParticleType {
    NONE,
    FAVORITE,
    WATCHLIST,
    WATCHED,
    COLLECTION
}

private data class MediaMicroParticle(
    val initialX: Float,
    val driftX: Float,
    val targetY: Float,
    val size: Dp,
    val color: Color,
    val delayMs: Int,
    val rotation: Float,
    val icon: ImageVector? = null,
    val isCircle: Boolean = false
)

private fun createParticles(
    type: MediaActionParticleType,
    colorScheme: ColorScheme
): List<MediaMicroParticle> {
    val (primaryIcon, secIcon, baseColor) = when (type) {
        MediaActionParticleType.FAVORITE -> Triple(
            Icons.Rounded.Favorite,
            Icons.Rounded.Star,
            colorScheme.error
        )

        MediaActionParticleType.WATCHLIST -> Triple(
            Icons.Rounded.Bookmark,
            Icons.Rounded.Star,
            colorScheme.primary
        )

        MediaActionParticleType.WATCHED -> Triple(
            Icons.Rounded.Check,
            Icons.Rounded.Star,
            colorScheme.tertiary
        )

        MediaActionParticleType.COLLECTION -> Triple(
            Icons.Rounded.FolderSpecial,
            Icons.Rounded.Star,
            colorScheme.secondary
        )

        MediaActionParticleType.NONE -> return emptyList()
    }

    return listOf(
        // Primary main icon - upward center float
        MediaMicroParticle(
            initialX = 0f,
            driftX = -4f,
            targetY = -78f,
            size = 24.dp,
            color = baseColor,
            delayMs = 0,
            rotation = 0f,
            icon = primaryIcon
        ),
        // Secondary icon - mid-left arc
        MediaMicroParticle(
            initialX = -6f,
            driftX = -32f,
            targetY = -66f,
            size = 18.dp,
            color = baseColor,
            delayMs = 25,
            rotation = -18f,
            icon = primaryIcon
        ),
        // Secondary icon - mid-right arc
        MediaMicroParticle(
            initialX = 6f,
            driftX = 26f,
            targetY = -70f,
            size = 18.dp,
            color = baseColor,
            delayMs = 40,
            rotation = 18f,
            icon = primaryIcon
        ),
        // Star - wide left inward burst
        MediaMicroParticle(
            initialX = -12f,
            driftX = -48f,
            targetY = -54f,
            size = 15.dp,
            color = baseColor.copy(alpha = 0.9f),
            delayMs = 60,
            rotation = -26f,
            icon = secIcon
        ),
        // Star - wide right burst
        MediaMicroParticle(
            initialX = 12f,
            driftX = 38f,
            targetY = -58f,
            size = 15.dp,
            color = baseColor.copy(alpha = 0.9f),
            delayMs = 75,
            rotation = 26f,
            icon = secIcon
        ),
        // Star - high crown accent
        MediaMicroParticle(
            initialX = 0f,
            driftX = -10f,
            targetY = -92f,
            size = 16.dp,
            color = baseColor.copy(alpha = 0.85f),
            delayMs = 90,
            rotation = 14f,
            icon = Icons.Rounded.Star
        ),
        // Sparkle glow dot - left
        MediaMicroParticle(
            initialX = -10f,
            driftX = -38f,
            targetY = -42f,
            size = 8.dp,
            color = baseColor.copy(alpha = 0.85f),
            delayMs = 30,
            rotation = 0f,
            isCircle = true
        ),
        // Sparkle glow dot - right
        MediaMicroParticle(
            initialX = 10f,
            driftX = 32f,
            targetY = -46f,
            size = 8.dp,
            color = baseColor.copy(alpha = 0.85f),
            delayMs = 50,
            rotation = 0f,
            isCircle = true
        ),
        // High floating micro dot
        MediaMicroParticle(
            initialX = -4f,
            driftX = -16f,
            targetY = -84f,
            size = 6.dp,
            color = baseColor.copy(alpha = 0.75f),
            delayMs = 100,
            rotation = 0f,
            isCircle = true
        )
    )
}

@Composable
fun MediaActionFeedbackParticles(
    type: MediaActionParticleType,
    triggerKey: Long,
    modifier: Modifier = Modifier
) {
    if (type == MediaActionParticleType.NONE || triggerKey == 0L) return

    val colorScheme = MaterialTheme.colorScheme
    val particles = remember(triggerKey, type, colorScheme) {
        createParticles(type, colorScheme)
    }

    val animProgress = remember(triggerKey, type) { Animatable(0f) }

    LaunchedEffect(triggerKey, type) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    if (animProgress.value < 1f && particles.isNotEmpty()) {
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                clippingEnabled = false
            )
        ) {
            Box(
                modifier = modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                val totalDuration = 900f

                particles.forEach { particle ->
                    val adjustedProgress =
                        ((animProgress.value * totalDuration - particle.delayMs) /
                                (totalDuration - particle.delayMs)).coerceIn(0f, 1f)

                    val alpha = if (adjustedProgress < 0.10f) {
                        adjustedProgress / 0.10f
                    } else if (adjustedProgress < 0.60f) {
                        1f
                    } else {
                        (1f - (adjustedProgress - 0.60f) / 0.40f).coerceIn(0f, 1f)
                    }

                    val scale = if (adjustedProgress < 0.18f) {
                        (adjustedProgress / 0.18f) * 1.30f
                    } else {
                        1.30f - (adjustedProgress - 0.18f) * 0.30f
                    }

                    val currentX =
                        particle.initialX + (particle.driftX - particle.initialX) * adjustedProgress
                    val currentY = particle.targetY * adjustedProgress

                    if (particle.icon != null) {
                        Icon(
                            imageVector = particle.icon,
                            contentDescription = null,
                            tint = particle.color,
                            modifier = Modifier
                                .size(particle.size)
                                .graphicsLayer {
                                    translationX = currentX.dp.toPx()
                                    translationY = currentY.dp.toPx()
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    rotationZ = particle.rotation + (adjustedProgress * 35f)
                                }
                        )
                    } else if (particle.isCircle) {
                        Box(
                            modifier = Modifier
                                .size(particle.size)
                                .graphicsLayer {
                                    translationX = currentX.dp.toPx()
                                    translationY = currentY.dp.toPx()
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                }
                                .clip(CircleShape)
                                .background(particle.color)
                        )
                    }
                }
            }
        }
    }
}
