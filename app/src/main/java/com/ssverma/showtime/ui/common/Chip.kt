package com.ssverma.showtime.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    shape: Shape = RoundedCornerShape(percent = 50),
    alpha: Float = 0.32f,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
            .background(
                color = backgroundColor.copy(alpha = alpha),
                shape = shape,
            )
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = backgroundColor.copy(alpha = 0.16f)
                ),
                shape = shape
            )
            .clickable(
                indication = rememberRipple(bounded = false),
                interactionSource = MutableInteractionSource()
            ) { onClick() }
            .padding(vertical = 10.dp, horizontal = 20.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
            color = contentColorFor(backgroundColor = backgroundColor)
        )
    }
}