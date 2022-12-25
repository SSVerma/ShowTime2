package com.ssverma.core.ui.component

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.edgeToEdgeSpacing() = this
    .then(statusBarsPadding())
    .then(navigationBarsPadding())

fun Modifier.scrim(
    colors: List<Color>
): Modifier = drawWithContent {
    drawContent()
    drawRect(Brush.verticalGradient(colors))
}