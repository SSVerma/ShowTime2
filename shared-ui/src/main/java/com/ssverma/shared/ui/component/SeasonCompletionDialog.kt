package com.ssverma.shared.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.trakt.CompletedShowDialogState
import com.ssverma.shared.ui.R

@Composable
fun SeasonCompletionDialog(
    state: CompletedShowDialogState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.medium)
        ) {
            // Background / Overlaid Confetti Particle Fountain
            DialogCelebrationParticles()

            // Centered Celebration Modal Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(MaterialTheme.spacing.large)
                ) {
                    // Poster Thumbnail with Golden Glow Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        val posterUrl = state.showPosterPath
                        if (!posterUrl.isNullOrBlank()) {
                            NetworkImage(
                                url = posterUrl,
                                contentDescription = state.showTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Tv,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Floating Trophy Badge
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    // Title
                    Text(
                        text = stringResource(R.string.season_complete_dialog_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    // Message
                    Text(
                        text = stringResource(
                            R.string.season_complete_dialog_all_caught_up,
                            state.showTitle
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    // Progress Section (100% full)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(
                                horizontal = MaterialTheme.spacing.medium,
                                vertical = MaterialTheme.spacing.small
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.season_complete_dialog_episodes_completed,
                                    state.totalCompleted,
                                    state.totalAired
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "100%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.tertiary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                    // Action Button
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.season_complete_dialog_action),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogCelebrationParticles() {
    val particles = remember {
        listOf(
            DialogParticle(
                -60f,
                -120f,
                -220f,
                16.dp,
                Color(0xFFFFD700),
                0,
                -30f,
                Icons.Rounded.Star
            ),
            DialogParticle(0f, 20f, -260f, 18.dp, Color(0xFF4CAF50), 30, 15f, Icons.Rounded.Check),
            DialogParticle(
                50f,
                110f,
                -210f,
                15.dp,
                Color(0xFFED1C24),
                60,
                35f,
                Icons.Rounded.Favorite
            ),
            DialogParticle(
                -30f,
                -80f,
                -240f,
                14.dp,
                Color(0xFF2196F3),
                20,
                -15f,
                Icons.Rounded.Star
            ),
            DialogParticle(30f, 80f, -230f, 16.dp, Color(0xFFFF9800), 50, 25f, Icons.Rounded.Star),
            DialogParticle(
                -80f,
                -140f,
                -180f,
                13.dp,
                Color(0xFFE91E63),
                80,
                -45f,
                Icons.Rounded.Check
            ),
            DialogParticle(70f, 130f, -190f, 14.dp, Color(0xFF9C27B0), 70, 45f, Icons.Rounded.Star),
            DialogParticle(
                -10f,
                -30f,
                -270f,
                16.dp,
                Color(0xFF00BCD4),
                40,
                -10f,
                Icons.Rounded.Favorite
            ),
            DialogParticle(20f, 50f, -250f, 17.dp, Color(0xFFFFEB3B), 10, 20f, Icons.Rounded.Star),
            DialogParticle(
                -45f,
                -100f,
                -200f,
                12.dp,
                Color(0xFF8BC34A),
                90,
                -25f,
                Icons.Rounded.Check
            )
        )
    }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing)
        )
    }

    if (animProgress.value < 1f) {
        Box(
            modifier = Modifier.size(340.dp),
            contentAlignment = Alignment.Center
        ) {
            particles.forEach { particle ->
                val totalDuration = 1600f
                val adjustedProgress = ((animProgress.value * totalDuration - particle.delay) /
                        (totalDuration - particle.delay)).coerceIn(0f, 1f)

                val alpha = if (adjustedProgress < 0.15f) {
                    adjustedProgress / 0.15f
                } else if (adjustedProgress < 0.65f) {
                    1f
                } else {
                    (1f - (adjustedProgress - 0.65f) / 0.35f).coerceIn(0f, 1f)
                }

                val scale = if (adjustedProgress < 0.2f) {
                    (adjustedProgress / 0.2f) * 1.3f
                } else {
                    1.3f - (adjustedProgress - 0.2f) * 0.4f
                }

                val currentX =
                    particle.offsetX + (particle.driftX - particle.offsetX) * adjustedProgress
                val currentY = particle.targetY * adjustedProgress

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
                            rotationZ = particle.rotation + (adjustedProgress * 60f)
                        }
                )
            }
        }
    }
}

private data class DialogParticle(
    val offsetX: Float,
    val driftX: Float,
    val targetY: Float,
    val size: Dp,
    val color: Color,
    val delay: Int,
    val rotation: Float,
    val icon: ImageVector
)
