package com.ssverma.core.ui.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@Composable
fun Popup(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    tonalElevation: Dp = 3.dp,
    shadowElevation: Dp = 6.dp,
    border: BorderStroke? = BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    ),
    offset: DpOffset = DpOffset(0.dp, 8.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    anchorContent: @Composable BoxScope.() -> Unit,
    menuItems: @Composable ColumnScope.() -> Unit
) {
    val expanded = remember { mutableStateOf(false) }

    Popup(
        modifier = modifier,
        expandState = expanded,
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        offset = offset,
        properties = properties,
        anchorContent = anchorContent,
        menuItems = menuItems
    )
}

@Composable
fun Popup(
    modifier: Modifier = Modifier,
    expandState: MutableState<Boolean> = remember { mutableStateOf(false) },
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    tonalElevation: Dp = 3.dp,
    shadowElevation: Dp = 6.dp,
    border: BorderStroke? = BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    ),
    offset: DpOffset = DpOffset(0.dp, 8.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    anchorContent: @Composable BoxScope.() -> Unit,
    menuItems: @Composable ColumnScope.() -> Unit
) {
    var expanded by expandState

    Box(modifier) {
        Box(Modifier.clickable { expanded = true }) {
            anchorContent()
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation,
            border = border,
            offset = offset,
            properties = properties,
            modifier = Modifier
                .widthIn(min = 220.dp)
                .heightIn(max = 420.dp),
            content = menuItems
        )
    }
}