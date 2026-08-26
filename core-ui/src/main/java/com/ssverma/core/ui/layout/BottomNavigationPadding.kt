package com.ssverma.core.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object FloatingBottomBarDefaults {
    val Height: Dp = 56.dp
    val BottomMargin: Dp = 16.dp
    val ContentClearance: Dp = 16.dp
}

object FloatingTopSearchBarDefaults {
    val Height: Dp = 52.dp
    val VerticalMargin: Dp = 6.dp
    val ContentClearance: Dp = 8.dp
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

/**
 * Calculates dynamic bottom bar height including system navigation bar insets and clearances.
 */
@Composable
fun rememberFloatingBottomBarHeight(
    extraSpacing: Dp = FloatingBottomBarDefaults.ContentClearance
): Dp {
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val bottomInset = navBarsPadding.calculateBottomPadding()

    return remember(bottomInset, extraSpacing) {
        FloatingBottomBarDefaults.Height + FloatingBottomBarDefaults.BottomMargin + extraSpacing + bottomInset
    }
}

/**
 * Calculates dynamic top and bottom content padding for pages containing both the floating top search bar
 * and the floating bottom navigation bar, allowing content to render and scroll edge-to-edge behind both bars.
 */
@Composable
fun rememberFloatingBarsPadding(
    includeBottomBarPadding: Boolean = true,
    extraBottomSpacing: Dp = FloatingBottomBarDefaults.ContentClearance,
    extraTopSpacing: Dp = FloatingTopSearchBarDefaults.ContentClearance,
    start: Dp = 0.dp,
    end: Dp = 0.dp
): PaddingValues {
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val statusBarsPadding = WindowInsets.statusBars.asPaddingValues()
    val bottomInset = navBarsPadding.calculateBottomPadding()
    val topInset = statusBarsPadding.calculateTopPadding()

    return remember(
        bottomInset,
        topInset,
        includeBottomBarPadding,
        extraBottomSpacing,
        extraTopSpacing,
        start,
        end
    ) {
        PaddingValues(
            start = start,
            top = FloatingTopSearchBarDefaults.Height + (FloatingTopSearchBarDefaults.VerticalMargin * 2) + extraTopSpacing + topInset,
            end = end,
            bottom = if (includeBottomBarPadding) {
                FloatingBottomBarDefaults.Height + FloatingBottomBarDefaults.BottomMargin + extraBottomSpacing + bottomInset
            } else {
                0.dp
            }
        )
    }
}

