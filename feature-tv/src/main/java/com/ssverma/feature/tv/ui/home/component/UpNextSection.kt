package com.ssverma.feature.tv.ui.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.layout.aspectRatio
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch

@Composable
fun UpNextSection(
    upNextEpisodes: List<TraktUpNextEpisode>,
    onUpNextEpisodeClick: (showTmdbId: Int, seasonNumber: Int) -> Unit,
    onMarkWatchedClick: (showTmdbId: Int, seasonNumber: Int, episodeNumber: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFED1C24).copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Tv,
                            contentDescription = null,
                            tint = Color(0xFFED1C24),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.up_next_to_watch),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Horizontal Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            items(
                items = upNextEpisodes,
                key = { it.showTmdbId }
            ) { episode ->
                UpNextCard(
                    episode = episode,
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(durationMillis = 300),
                        fadeOutSpec = tween(durationMillis = 350),
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    onClick = { onUpNextEpisodeClick(episode.showTmdbId, episode.seasonNumber) },
                    onMarkWatched = {
                        onMarkWatchedClick(
                            episode.showTmdbId,
                            episode.seasonNumber,
                            episode.episodeNumber
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun UpNextCard(
    episode: TraktUpNextEpisode,
    onClick: () -> Unit,
    onMarkWatched: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val buttonScale = remember { Animatable(1f) }
    var burstTrigger by remember { mutableIntStateOf(0) }
    val isCompleted = episode.totalCompleted >= episode.totalAired

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            burstTrigger++
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = episode.progressPercentage,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "UpNextProgressAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) {
            Color(0xFFFFD700).copy(alpha = 0.8f) // Gold celebration border
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "UpNextBorderColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        OutlinedCard(
            modifier = Modifier
                .width(300.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            border = BorderStroke(width = 1.dp, color = borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini Artwork / Poster Thumbnail
                val posterUrl = episode.showPosterPath
                if (!posterUrl.isNullOrBlank()) {
                    NetworkImage(
                        url = posterUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(62.dp)
                            .aspectRatio(TmdbPosterAspectRatio)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Show Title & Episode Code Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = episode.showTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCompleted) {
                                Color(0xFFFFD700).copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                text = if (isCompleted) {
                                    stringResource(R.string.caught_up)
                                } else {
                                    stringResource(
                                        R.string.episode_code,
                                        episode.seasonNumber,
                                        episode.episodeNumber
                                    )
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) {
                                    Color(0xFFD48800)
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    }

                    // Episode Title
                    val rawTitle = episode.episodeTitle
                    val displayEpisodeTitle = if (isCompleted) {
                        stringResource(R.string.season_completed)
                    } else if (!rawTitle.isNullOrBlank()) {
                        rawTitle
                    } else {
                        "Episode ${episode.episodeNumber}"
                    }

                    Text(
                        text = displayEpisodeTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 1.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (isCompleted) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress Count & 1-Tap Watched Action
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${episode.totalCompleted}/${episode.totalAired} eps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilledTonalButton(
                            onClick = {
                                coroutineScope.launch {
                                    buttonScale.animateTo(0.85f, animationSpec = tween(50))
                                    buttonScale.animateTo(1f, animationSpec = spring())
                                }
                                burstTrigger++
                                onMarkWatched()
                            },
                            enabled = !isCompleted,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(26.dp)
                                .graphicsLayer {
                                    scaleX = buttonScale.value
                                    scaleY = buttonScale.value
                                },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isCompleted) {
                                    Color(0xFFFFD700).copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                                contentColor = if (isCompleted) {
                                    Color(0xFFD48800)
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isCompleted) {
                                    stringResource(R.string.mark_watched_done)
                                } else {
                                    stringResource(R.string.mark_watched)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Festive Sparkles & Confetti Burst
        CelebrationBurst(trigger = burstTrigger)
    }
}

@Composable
private fun CelebrationBurst(trigger: Int) {
    if (trigger == 0) return

    val particles = remember(trigger) {
        listOf(
            CelebrationParticle(
                -30f,
                -60f,
                -80f,
                14.dp,
                Color(0xFFFFD700),
                0,
                -25f,
                Icons.Rounded.Star
            ),
            CelebrationParticle(
                0f,
                10f,
                -110f,
                16.dp,
                Color(0xFF4CAF50),
                40,
                15f,
                Icons.Rounded.Check
            ),
            CelebrationParticle(
                25f,
                55f,
                -85f,
                13.dp,
                Color(0xFFED1C24),
                80,
                30f,
                Icons.Rounded.Favorite
            ),
            CelebrationParticle(
                -15f,
                -40f,
                -100f,
                12.dp,
                Color(0xFF2196F3),
                20,
                -15f,
                Icons.Rounded.Star
            ),
            CelebrationParticle(
                15f,
                40f,
                -95f,
                15.dp,
                Color(0xFFFF9800),
                60,
                20f,
                Icons.Rounded.Star
            ),
            CelebrationParticle(
                -40f,
                -75f,
                -70f,
                11.dp,
                Color(0xFFE91E63),
                100,
                -35f,
                Icons.Rounded.Check
            ),
            CelebrationParticle(
                35f,
                70f,
                -75f,
                12.dp,
                Color(0xFF9C27B0),
                70,
                40f,
                Icons.Rounded.Star
            )
        )
    }

    val animProgress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
    }

    if (animProgress.value < 1f) {
        Popup(
            alignment = Alignment.BottomCenter,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                clippingEnabled = false
            )
        ) {
            Box(
                modifier = Modifier.size(width = 180.dp, height = 180.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                particles.forEach { particle ->
                    val totalDuration = 1400f
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
                        (adjustedProgress / 0.2f) * 1.25f
                    } else {
                        1.25f - (adjustedProgress - 0.2f) * 0.35f
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
                                rotationZ = particle.rotation + (adjustedProgress * 45f)
                            }
                    )
                }
            }
        }
    }
}

private data class CelebrationParticle(
    val offsetX: Float,
    val driftX: Float,
    val targetY: Float,
    val size: Dp,
    val color: Color,
    val delay: Int,
    val rotation: Float,
    val icon: ImageVector
)
