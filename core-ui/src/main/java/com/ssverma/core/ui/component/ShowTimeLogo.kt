package com.ssverma.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun ShowTimeLogo(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    playColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    Surface(
        shape = shape,
        color = containerColor,
        modifier = modifier.aspectRatio(1f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Scale factor relative to 48x48 viewport
                val sx = w / 48f
                val sy = h / 48f

                // Top clapperboard stripes (M36,14 L33,20 L30,20 L33,14 L27,14 L24,20 L21,20 L24,14 L18,14 L15,20 L12,20 L12,14 C12,12.9 12.9,12 14,12 L34,12 C35.1,12 36,12.9 36,14 Z)
                val clapperPath = Path().apply {
                    moveTo(36f * sx, 14f * sy)
                    lineTo(33f * sx, 20f * sy)
                    lineTo(30f * sx, 20f * sy)
                    lineTo(33f * sx, 14f * sy)
                    lineTo(27f * sx, 14f * sy)
                    lineTo(24f * sx, 20f * sy)
                    lineTo(21f * sx, 20f * sy)
                    lineTo(24f * sx, 14f * sy)
                    lineTo(18f * sx, 14f * sy)
                    lineTo(15f * sx, 20f * sy)
                    lineTo(12f * sx, 20f * sy)
                    lineTo(12f * sx, 14f * sy)
                    cubicTo(12f * sx, 12.9f * sy, 12.9f * sx, 12f * sy, 14f * sx, 12f * sy)
                    lineTo(34f * sx, 12f * sy)
                    cubicTo(35.1f * sx, 12f * sy, 36f * sx, 12.9f * sy, 36f * sx, 14f * sy)
                    close()
                }
                drawPath(clapperPath, contentColor)

                // Lower Body (M12,22 L36,22 L36,34 C36,35.1 35.1,36 34,36 L14,36 C12.9,36 12,35.1 12,34 L12,22 Z)
                val bodyPath = Path().apply {
                    moveTo(12f * sx, 22f * sy)
                    lineTo(36f * sx, 22f * sy)
                    lineTo(36f * sx, 34f * sy)
                    cubicTo(36f * sx, 35.1f * sy, 35.1f * sx, 36f * sy, 34f * sx, 36f * sy)
                    lineTo(14f * sx, 36f * sy)
                    cubicTo(12.9f * sx, 36f * sy, 12f * sx, 35.1f * sy, 12f * sx, 34f * sy)
                    lineTo(12f * sx, 22f * sy)
                    close()
                }
                drawPath(bodyPath, contentColor)

                // Center Play Symbol (M21,25 L29,29 L21,33 Z)
                val playPath = Path().apply {
                    moveTo(21f * sx, 25f * sy)
                    lineTo(29f * sx, 29f * sy)
                    lineTo(21f * sx, 33f * sy)
                    close()
                }
                drawPath(playPath, playColor)
            }
        }
    }
}
