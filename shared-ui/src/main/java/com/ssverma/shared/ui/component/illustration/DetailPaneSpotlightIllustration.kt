package com.ssverma.shared.ui.component.illustration

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ui.R

/**
 * Theme-aware empty state displayed in the adaptive detail pane before a title is selected.
 */
@Composable
fun DetailPaneEmptyState(
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.detail_pane_empty_title),
    subtitle: String = stringResource(id = R.string.detail_pane_empty_subtitle)
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.large)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.widthIn(max = DetailEmptyStateDefaults.MaxContentWidth)
            ) {
                DetailPaneSpotlightIllustration()

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Animated cinema spotlight illustration driven purely by semantic [MaterialTheme.colorScheme]
 * tokens for seamless adaptation across Light, Dark, and OLED themes.
 */
@Composable
fun DetailPaneSpotlightIllustration(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DetailPaneSpotlight")
    val floatProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = DetailEmptyStateDefaults.FloatDurationMs,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CardFloat"
    )
    val orbitProgress by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = DetailEmptyStateDefaults.OrbitDurationMs,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeOrbit"
    )
    val starPulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = DetailEmptyStateDefaults.StarPulseDurationMs,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarPulse"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(DetailEmptyStateDefaults.IllustrationSize)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(x = size.width / 2f, y = size.height / 2f)
            val maxRadius = size.minDimension / 2f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.16f),
                        secondaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            val ringFractions = listOf(0.56f, 0.76f, 0.94f)
            ringFractions.forEachIndexed { ringIndex, ringFraction ->
                val ringAlpha = 0.24f - (ringIndex * 0.06f)
                drawCircle(
                    color = outlineColor.copy(alpha = ringAlpha),
                    radius = maxRadius * ringFraction,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(DetailEmptyStateDefaults.CardCornerRadius),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            shadowElevation = DetailEmptyStateDefaults.CardElevation,
            modifier = Modifier
                .widthIn(max = DetailEmptyStateDefaults.CardWidth)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = floatProgress * DetailEmptyStateDefaults.FloatAmplitudePx
                }
        ) {
            Column(
                modifier = Modifier.padding(MaterialTheme.spacing.medium)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
                ) {
                    Surface(
                        shape = RoundedCornerShape(DetailEmptyStateDefaults.IconBadgeCornerRadius),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(DetailEmptyStateDefaults.HeaderIconContainerSize)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Movie,
                                contentDescription = null,
                                modifier = Modifier.size(DetailEmptyStateDefaults.HeaderIconSize)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            text = stringResource(id = R.string.detail_pane_preview_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.spacing.smallMedium,
                                vertical = MaterialTheme.spacing.extraSmall
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                Surface(
                    shape = RoundedCornerShape(DetailEmptyStateDefaults.SkeletonCornerRadius),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.82f)
                        .height(DetailEmptyStateDefaults.TitleBarHeight)
                ) {}

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(DetailEmptyStateDefaults.StarCount) { starIndex ->
                        val phaseFactor = (starPulseProgress + starIndex * 0.14f) % 1f
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(DetailEmptyStateDefaults.StarSize)
                                .graphicsLayer {
                                    alpha = 0.72f + 0.28f * phaseFactor
                                    val starScale = 0.94f + 0.08f * phaseFactor
                                    scaleX = starScale
                                    scaleY = starScale
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .width(DetailEmptyStateDefaults.PrimaryPillWidth)
                            .height(DetailEmptyStateDefaults.PillHeight)
                    ) {}

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        modifier = Modifier
                            .width(DetailEmptyStateDefaults.SecondaryPillWidth)
                            .height(DetailEmptyStateDefaults.PillHeight)
                    ) {}
                }
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            shadowElevation = DetailEmptyStateDefaults.BadgeElevation,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = MaterialTheme.spacing.large,
                    end = MaterialTheme.spacing.medium
                )
                .size(DetailEmptyStateDefaults.OrbitBadgeSize)
                .graphicsLayer {
                    translationY = orbitProgress * DetailEmptyStateDefaults.OrbitAmplitudePx
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.LiveTv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(DetailEmptyStateDefaults.OrbitIconSize)
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shadowElevation = DetailEmptyStateDefaults.BadgeElevation,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    bottom = MaterialTheme.spacing.large,
                    start = MaterialTheme.spacing.medium
                )
                .size(DetailEmptyStateDefaults.OrbitBadgeSize)
                .graphicsLayer {
                    translationY = -orbitProgress * DetailEmptyStateDefaults.OrbitAmplitudePx
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(DetailEmptyStateDefaults.OrbitIconSize)
                )
            }
        }
    }
}

private object DetailEmptyStateDefaults {
    val MaxContentWidth = 360.dp
    val IllustrationSize = 260.dp
    val CardWidth = 228.dp
    val CardCornerRadius = 20.dp
    val CardElevation = 6.dp
    val BadgeElevation = 4.dp
    val IconBadgeCornerRadius = 12.dp
    val HeaderIconContainerSize = 38.dp
    val HeaderIconSize = 20.dp
    val SkeletonCornerRadius = 6.dp
    val TitleBarHeight = 14.dp
    val StarCount = 5
    val StarSize = 16.dp
    val PrimaryPillWidth = 76.dp
    val SecondaryPillWidth = 54.dp
    val PillHeight = 10.dp
    val OrbitBadgeSize = 44.dp
    val OrbitIconSize = 20.dp
    const val FloatAmplitudePx = 10f
    const val OrbitAmplitudePx = 14f
    const val FloatDurationMs = 2800
    const val OrbitDurationMs = 3400
    const val StarPulseDurationMs = 1900
}
