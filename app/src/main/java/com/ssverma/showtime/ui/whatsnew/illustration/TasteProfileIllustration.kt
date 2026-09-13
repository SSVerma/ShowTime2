package com.ssverma.showtime.ui.whatsnew.illustration

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TasteProfileIllustration(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "taste_profile_motion")

    // Radar breathing pulse
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_breathing"
    )

    // Floating badge bobbing
    val badgeFloatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_float_y"
    )

    val radarColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        // Atmospheric Ambient Halo
        Box(
            modifier = Modifier
                .size(190.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            radarColor.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Geometric Radar Canvas
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .fillMaxSize()
        ) {
            val centerOffset = center
            val maxRadius = size.minDimension / 2 * 0.9f
            val vertexCount = 5
            val angleStep = (2 * Math.PI / vertexCount).toFloat()
            val startAngle = (-Math.PI / 2).toFloat() // Pointing straight up

            // 1. Draw Concentric Pentagonal Grids (3 levels)
            val gridLevels = listOf(0.4f, 0.7f, 1.0f)
            gridLevels.forEach { level ->
                val gridPath = Path()
                for (i in 0 until vertexCount) {
                    val angle = startAngle + i * angleStep
                    val r = maxRadius * level
                    val x = centerOffset.x + r * cos(angle)
                    val y = centerOffset.y + r * sin(angle)
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(
                    path = gridPath,
                    color = gridColor.copy(alpha = 0.25f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 2. Draw 5 Spokes from Center to Outer Vertices
            for (i in 0 until vertexCount) {
                val angle = startAngle + i * angleStep
                val x = centerOffset.x + maxRadius * cos(angle)
                val y = centerOffset.y + maxRadius * sin(angle)
                drawLine(
                    color = gridColor.copy(alpha = 0.2f),
                    start = centerOffset,
                    end = androidx.compose.ui.geometry.Offset(x, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 3. Draw The Dynamic Taste DNA Polygon (Pulsing)
            val dataWeights = listOf(0.88f, 0.72f, 0.94f, 0.65f, 0.82f)
            val dataPath = Path()
            val vertexOffsets = mutableListOf<androidx.compose.ui.geometry.Offset>()

            for (i in 0 until vertexCount) {
                val angle = startAngle + i * angleStep
                val r = maxRadius * dataWeights[i] * radarPulse
                val x = centerOffset.x + r * cos(angle)
                val y = centerOffset.y + r * sin(angle)
                val offset = androidx.compose.ui.geometry.Offset(x, y)
                vertexOffsets.add(offset)
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            // Fill & Outline
            drawPath(
                path = dataPath,
                color = radarColor.copy(alpha = 0.28f)
            )
            drawPath(
                path = dataPath,
                color = radarColor,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw Glowing Vertex Nodes
            vertexOffsets.forEach { nodeOffset ->
                drawCircle(
                    color = radarColor,
                    radius = 4.dp.toPx(),
                    center = nodeOffset
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.8.dp.toPx(),
                    center = nodeOffset
                )
            }
        }

        // Floating Cinema DNA Badge (Bottom Right)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shadowElevation = 6.dp,
            modifier = Modifier
                .offset(x = 64.dp, y = (42 + badgeFloatY).dp)
                .border(
                    width = 1.dp,
                    color = radarColor.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = null,
                    tint = radarColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(32.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(radarColor.copy(alpha = 0.4f))
                )
            }
        }

        // Floating Insights Indicator (Top Left)
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 4.dp,
            modifier = Modifier
                .offset(x = (-68).dp, y = (-42 - badgeFloatY).dp)
                .size(34.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Insights,
                    contentDescription = null,
                    tint = radarColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
