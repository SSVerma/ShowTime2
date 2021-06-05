package com.ssverma.showtime.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Chip(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ChipDefaults.ContentPadding,
    content: @Composable () -> Unit
) {
    ClickThroughSurface(
        modifier = modifier,
        color = backgroundColor,
        shape = shape,
        contentColor = contentColor,
        border = border,
        elevation = elevation
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
            CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = content)
        }
    }
}

@Composable
fun DefaultSelectedChip(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    backgroundColor: Color = MaterialTheme.colors.onSurface,
    contentColor: Color = contentColorFor(backgroundColor),
    border: BorderStroke? = null,
    elevation: Dp = 4.dp,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ChipDefaults.ContentPadding,
    content: @Composable () -> Unit
) {
    Chip(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        border = border,
        elevation = elevation,
        enabled = enabled,
        contentPadding = contentPadding,
        content = content
    )
}

object ChipDefaults {
    val ContentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
}

@Preview
@Composable
fun SelectedChipPreview() {
    DefaultSelectedChip {
        Text(text = "Text")
    }
}
