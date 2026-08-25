package com.ssverma.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
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

enum class GameParticleType {
    NONE,
    SUCCESS_CONFETTI,
    WRONG_GUESS_EMBER,
    GAME_OVER_LOST
}

data class MicroParticle(
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

@Composable
fun GameFeedbackParticles(
    effectType: GameParticleType,
    triggerKey: Long,
    modifier: Modifier = Modifier
) {
    if (effectType == GameParticleType.NONE || triggerKey == 0L) return

    val particles = remember(triggerKey, effectType) {
        when (effectType) {
            GameParticleType.WRONG_GUESS_EMBER -> {
                // Clean micro-action icon bursts like tile menu actions
                listOf(
                    MicroParticle(
                        -35f,
                        -70f,
                        -80f,
                        18.dp,
                        Color(0xFFE53935),
                        0,
                        -20f,
                        Icons.Rounded.Close
                    ),
                    MicroParticle(
                        0f,
                        15f,
                        -110f,
                        20.dp,
                        Color(0xFFFF1744),
                        30,
                        15f,
                        Icons.Rounded.Cancel
                    ),
                    MicroParticle(
                        35f,
                        75f,
                        -85f,
                        17.dp,
                        Color(0xFFFF5722),
                        60,
                        25f,
                        Icons.Rounded.Close
                    ),
                    MicroParticle(
                        -15f,
                        -35f,
                        -95f,
                        10.dp,
                        Color(0xFFFF7043),
                        15,
                        0f,
                        isCircle = true
                    ),
                    MicroParticle(
                        20f,
                        45f,
                        -90f,
                        11.dp,
                        Color(0xFFFFAB40),
                        45,
                        0f,
                        isCircle = true
                    ),
                    MicroParticle(
                        -50f,
                        -90f,
                        -65f,
                        16.dp,
                        Color(0xFFD32F2F),
                        80,
                        -35f,
                        Icons.Rounded.Close
                    ),
                    MicroParticle(
                        50f,
                        90f,
                        -70f,
                        16.dp,
                        Color(0xFFFF5252),
                        70,
                        30f,
                        Icons.Rounded.Cancel
                    ),
                    MicroParticle(
                        -5f,
                        -10f,
                        -120f,
                        8.dp,
                        Color(0xFFFF8A80),
                        50,
                        0f,
                        isCircle = true
                    )
                )
            }

            GameParticleType.SUCCESS_CONFETTI -> {
                // Festive celebration icon particles & stars
                listOf(
                    MicroParticle(
                        -40f,
                        -80f,
                        -120f,
                        22.dp,
                        Color(0xFFFFD700),
                        0,
                        -25f,
                        Icons.Rounded.Star
                    ),
                    MicroParticle(
                        0f,
                        10f,
                        -145f,
                        24.dp,
                        Color(0xFF4CAF50),
                        30,
                        15f,
                        Icons.Rounded.Check
                    ),
                    MicroParticle(
                        40f,
                        85f,
                        -125f,
                        22.dp,
                        Color(0xFF2196F3),
                        60,
                        30f,
                        Icons.Rounded.Movie
                    ),
                    MicroParticle(
                        -20f,
                        -50f,
                        -135f,
                        20.dp,
                        Color(0xFFE91E63),
                        20,
                        -15f,
                        Icons.Rounded.EmojiEvents
                    ),
                    MicroParticle(
                        20f,
                        50f,
                        -130f,
                        22.dp,
                        Color(0xFFFF9800),
                        45,
                        20f,
                        Icons.Rounded.Star
                    ),
                    MicroParticle(
                        -60f,
                        -110f,
                        -95f,
                        18.dp,
                        Color(0xFF00E676),
                        90,
                        -35f,
                        Icons.Rounded.Check
                    ),
                    MicroParticle(
                        60f,
                        110f,
                        -100f,
                        19.dp,
                        Color(0xFF9C27B0),
                        75,
                        40f,
                        Icons.Rounded.Star
                    ),
                    MicroParticle(
                        -10f,
                        -20f,
                        -155f,
                        14.dp,
                        Color(0xFFFFD700),
                        10,
                        0f,
                        isCircle = true
                    ),
                    MicroParticle(
                        10f,
                        25f,
                        -150f,
                        14.dp,
                        Color(0xFF00E676),
                        50,
                        0f,
                        isCircle = true
                    ),
                    MicroParticle(
                        -30f,
                        -65f,
                        -110f,
                        12.dp,
                        Color(0xFF2196F3),
                        80,
                        0f,
                        isCircle = true
                    ),
                    MicroParticle(
                        30f,
                        70f,
                        -115f,
                        12.dp,
                        Color(0xFFFF5722),
                        65,
                        0f,
                        isCircle = true
                    )
                )
            }

            GameParticleType.GAME_OVER_LOST -> {
                // Soft falling crimson & slate close icons
                listOf(
                    MicroParticle(
                        -30f,
                        -50f,
                        80f,
                        18.dp,
                        Color(0xFFD32F2F),
                        0,
                        -20f,
                        Icons.Rounded.Close
                    ),
                    MicroParticle(
                        0f,
                        10f,
                        110f,
                        18.dp,
                        Color(0xFF78909C),
                        40,
                        15f,
                        Icons.Rounded.Cancel
                    ),
                    MicroParticle(
                        30f,
                        55f,
                        85f,
                        18.dp,
                        Color(0xFF880E4F),
                        70,
                        25f,
                        Icons.Rounded.Close
                    ),
                    MicroParticle(
                        -15f,
                        -25f,
                        95f,
                        10.dp,
                        Color(0xFFE57373),
                        20,
                        0f,
                        isCircle = true
                    ),
                    MicroParticle(20f, 35f, 90f, 10.dp, Color(0xFF90A4AE), 50, 0f, isCircle = true)
                )
            }

            GameParticleType.NONE -> emptyList()
        }
    }

    val animProgress = remember(triggerKey, effectType) { Animatable(0f) }

    LaunchedEffect(triggerKey, effectType) {
        animProgress.snapTo(0f)
        val duration = when (effectType) {
            GameParticleType.SUCCESS_CONFETTI -> 1600
            GameParticleType.WRONG_GUESS_EMBER -> 1100
            GameParticleType.GAME_OVER_LOST -> 1400
            GameParticleType.NONE -> 0
        }
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        )
    }

    if (animProgress.value < 1f && particles.isNotEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val totalDuration = when (effectType) {
                GameParticleType.SUCCESS_CONFETTI -> 1600f
                GameParticleType.WRONG_GUESS_EMBER -> 1100f
                GameParticleType.GAME_OVER_LOST -> 1400f
                GameParticleType.NONE -> 1f
            }

            particles.forEach { particle ->
                val adjustedProgress = ((animProgress.value * totalDuration - particle.delayMs) /
                        (totalDuration - particle.delayMs)).coerceIn(0f, 1f)

                val alpha = if (adjustedProgress < 0.15f) {
                    adjustedProgress / 0.15f
                } else if (adjustedProgress < 0.60f) {
                    1f
                } else {
                    (1f - (adjustedProgress - 0.60f) / 0.40f).coerceIn(0f, 1f)
                }

                val scale = if (adjustedProgress < 0.25f) {
                    (adjustedProgress / 0.25f) * 1.25f
                } else {
                    1.25f - (adjustedProgress - 0.25f) * 0.35f
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
                                rotationZ = particle.rotation + (adjustedProgress * 50f)
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

@Composable
fun CelebrationParticles(
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    GameFeedbackParticles(
        effectType = if (trigger) GameParticleType.SUCCESS_CONFETTI else GameParticleType.NONE,
        triggerKey = if (trigger) 1L else 0L,
        modifier = modifier
    )
}
