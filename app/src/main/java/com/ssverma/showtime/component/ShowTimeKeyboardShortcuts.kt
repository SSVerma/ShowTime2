package com.ssverma.showtime.component

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.ssverma.showtime.navigation.ShowTimeRailNavItems
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItem

@Immutable
data class ShowTimeShortcutActions(
    val onOpenSearch: () -> Unit,
    val onSelectTopLevelTab: (ShowTimeTopLevelNavItem) -> Unit,
    val onOpenCinemaDiary: () -> Unit,
    val onOpenDiscovery: () -> Unit,
    val onOpenShortcutsHelp: () -> Unit,
    val onEscape: () -> Boolean
)

/**
 * Installs root-level desktop keyboard shortcuts while preserving normal typing inside focused
 * text inputs (via bubble-phase [onKeyEvent] for printable keys such as `/` and `?`).
 */
@Composable
fun Modifier.showTimeGlobalShortcuts(
    actions: ShowTimeShortcutActions,
    currentDestinationKey: Any?
): Modifier = composed {
    val rootFocusRequester = remember { FocusRequester() }
    var hierarchyHasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(currentDestinationKey, hierarchyHasFocus) {
        if (!hierarchyHasFocus) {
            runCatching { rootFocusRequester.requestFocus() }
        }
    }

    this
        .onFocusChanged { focusState ->
            hierarchyHasFocus = focusState.hasFocus
        }
        .focusRequester(rootFocusRequester)
        .focusable()
        .onPreviewKeyEvent { keyEvent ->
            handlePreviewShortcut(keyEvent = keyEvent, actions = actions)
        }
        .onKeyEvent { keyEvent ->
            handleBubbleShortcut(keyEvent = keyEvent, actions = actions)
        }
}

private fun handlePreviewShortcut(
    keyEvent: KeyEvent,
    actions: ShowTimeShortcutActions
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false

    val isCommandModifier = keyEvent.isCtrlPressed || keyEvent.isMetaPressed
    if (isCommandModifier && keyEvent.key == Key.K) {
        actions.onOpenSearch()
        return true
    }

    if (keyEvent.key == Key.Escape) {
        return actions.onEscape()
    }

    return false
}

private fun handleBubbleShortcut(
    keyEvent: KeyEvent,
    actions: ShowTimeShortcutActions
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false

    if (keyEvent.key == Key.Slash && !keyEvent.isCtrlPressed && !keyEvent.isMetaPressed && !keyEvent.isAltPressed) {
        if (keyEvent.isShiftPressed) {
            actions.onOpenShortcutsHelp()
        } else {
            actions.onOpenSearch()
        }
        return true
    }

    if (keyEvent.isAltPressed && !keyEvent.isCtrlPressed && !keyEvent.isMetaPressed) {
        val tabIndex = when (keyEvent.key) {
            Key.One -> 0
            Key.Two -> 1
            Key.Three -> 2
            Key.Four -> 3
            Key.Five -> 4
            else -> -1
        }
        if (tabIndex in ShowTimeRailNavItems.indices) {
            actions.onSelectTopLevelTab(ShowTimeRailNavItems[tabIndex])
            return true
        }
        if (keyEvent.key == Key.D) {
            actions.onOpenCinemaDiary()
            return true
        }
        if (keyEvent.key == Key.U) {
            actions.onOpenDiscovery()
            return true
        }
    }

    return false
}
