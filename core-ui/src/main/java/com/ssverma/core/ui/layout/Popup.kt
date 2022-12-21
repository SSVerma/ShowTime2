package com.ssverma.core.ui.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier


@Composable
fun Popup(
    modifier: Modifier = Modifier,
    anchorContent: @Composable BoxScope.() -> Unit,
    menuItems: @Composable ColumnScope.() -> Unit
) {
    val expanded = remember { mutableStateOf(false) }

    Popup(
        modifier = modifier,
        expandState = expanded,
        anchorContent = anchorContent,
        menuItems = menuItems
    )
}

@Composable
fun Popup(
    modifier: Modifier = Modifier,
    expandState: MutableState<Boolean> = remember { mutableStateOf(false) },
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
            content = menuItems
        )
    }
}