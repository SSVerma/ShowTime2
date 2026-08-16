package com.ssverma.core.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object FloatingBottomBarDefaults {
    val Height: Dp = 56.dp
    val BottomMargin: Dp = 16.dp
    val ContentClearance: Dp = 16.dp
}

/**
 * Calculates dynamic bottom content padding for pages containing the floating bottom navigation bar,
 * adjusting dynamically for system navigation bar insets (e.g., gesture navigation vs 3-button navigation).
 */
@Composable
fun rememberFloatingBottomBarPadding(
    extraSpacing: Dp = FloatingBottomBarDefaults.ContentClearance,
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp
): PaddingValues {
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val bottomInset = navBarsPadding.calculateBottomPadding()

    return remember(bottomInset, extraSpacing, start, top, end) {
        PaddingValues(
            start = start,
            top = top,
            end = end,
            bottom = FloatingBottomBarDefaults.Height + FloatingBottomBarDefaults.BottomMargin + extraSpacing + bottomInset
        )
    }
}
