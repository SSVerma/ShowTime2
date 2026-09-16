package com.ssverma.feature.library.ui.receipt.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.R

@Composable
fun CinemaReceiptIllustration(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cinema_receipt_motion")

    // Vertical floating animation for main receipt
    val receiptFloatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "receipt_float_y"
    )

    // Glowing laser scanner line sweep
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_progress"
    )

    // Floating badges counter-phase motion
    val badgeFloatY by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_float_y"
    )

    // Ambient glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val paperColor = MaterialTheme.colorScheme.surfaceContainer
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        // Atmospheric Ambient Halo
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        // Central Stylized Vintage Ticket / Receipt
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = paperColor,
            border = BorderStroke(
                width = 1.dp,
                color = dividerColor.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .width(148.dp)
                .height(172.dp)
                .graphicsLayer {
                    translationY = receiptFloatY * density
                }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.smallMedium)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Logo & Mini Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Movie,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Box(
                            modifier = Modifier
                                .height(5.dp)
                                .width(56.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(primaryColor.copy(alpha = 0.7f))
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    // Perforated Dashed Line
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                    ) {
                        drawLine(
                            color = dividerColor.copy(alpha = 0.7f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
                            strokeWidth = 1.5f
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    // Itemized Skeleton Receipt Lines
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .fillMaxWidth(0.6f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .fillMaxWidth(0.75f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Stylized Barcode
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                    ) {
                        val barCount = 28
                        val barWidth = size.width / (barCount * 1.4f)
                        var curX = 0f
                        for (i in 0 until barCount) {
                            val isThick = i % 3 == 0 || i % 7 == 0
                            val w = if (isThick) barWidth * 1.6f else barWidth
                            drawRect(
                                color = dividerColor,
                                topLeft = Offset(curX, 0f),
                                size = Size(w, size.height)
                            )
                            curX += w + (barWidth * 0.4f)
                            if (curX >= size.width) break
                        }
                    }
                }

                // Laser Scan Beam Sweep
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .graphicsLayer {
                            translationY = (172.dp.toPx() * laserProgress)
                        }
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    tertiaryColor.copy(alpha = 0.8f),
                                    Color.White,
                                    tertiaryColor.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // Floating "ADMIT ONE" Ticket Seal (Top Right)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = BorderStroke(
                width = 1.dp,
                color = primaryColor.copy(alpha = 0.45f)
            ),
            modifier = Modifier
                .offset(x = 64.dp, y = (-38 + badgeFloatY).dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ConfirmationNumber,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.receipt_admit_one),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Floating Receipt Icon Badge (Bottom Left)
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = BorderStroke(
                width = 1.dp,
                color = tertiaryColor.copy(alpha = 0.45f)
            ),
            modifier = Modifier
                .offset(x = (-66).dp, y = (42 - badgeFloatY).dp)
                .size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                    contentDescription = null,
                    tint = tertiaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
