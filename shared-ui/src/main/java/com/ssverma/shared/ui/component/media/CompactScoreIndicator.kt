package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompactScoreIndicator(score: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
    ) {
        CircularProgressIndicator(
            progress = { score / 100f },
            strokeWidth = 2.dp,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopStart)
        )
        Text(
            text = "${score.toInt()}%",
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
