package com.ssverma.shared.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.foundation.ClickThroughSurface

@Composable
fun ClickThroughFilterChip(
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    containerColor: Color = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    },
    labelColor: Color = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    },
    leadingIcon: @Composable (() -> Unit)? = null,
    selectedIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val finalContainerColor = if (enabled) containerColor else containerColor.copy(alpha = 0.38f)
    val finalContentColor = if (enabled) labelColor else labelColor.copy(alpha = 0.38f)

    val combinedModifier = if (onClick != null) {
        modifier.clickable(enabled = enabled, onClick = onClick)
    } else {
        modifier
    }

    ClickThroughSurface(
        modifier = combinedModifier,
        color = finalContainerColor,
        shape = shape,
        contentColor = finalContentColor,
        border = border,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected && selectedIcon != null) {
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    selectedIcon()
                }
            } else if (leadingIcon != null) {
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    leadingIcon()
                }
            }

            content()

            if (trailingIcon != null) {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    trailingIcon()
                }
            }
        }
    }
}
