package com.ssverma.showtime.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AppIcons = Icons.Default

fun Modifier.scrim(
    colors: List<Color>
): Modifier = drawWithContent {
    drawContent()
    drawRect(Brush.verticalGradient(colors))
}

@Composable
fun AppLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier.size(24.dp), strokeWidth = 2.dp)
}