package com.ssverma.core.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.adaptive.LocalAdaptiveLayoutInfo
import com.ssverma.core.ui.adaptive.NavigationChromeType

val LocalFloatingBarsVisible = compositionLocalOf { true }

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
    val chromeType = LocalAdaptiveLayoutInfo.current.navigationChromeType
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val bottomInset = navBarsPadding.calculateBottomPadding()

    return remember(chromeType, bottomInset, extraSpacing, start, top, end) {
        val bottomPadding = if (chromeType == NavigationChromeType.FloatingBottomBar) {
            FloatingBottomBarDefaults.Height + FloatingBottomBarDefaults.BottomMargin + extraSpacing + bottomInset
        } else {
            extraSpacing + bottomInset
        }
        PaddingValues(
            start = start,
            top = top,
            end = end,
            bottom = bottomPadding
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
    val chromeType = LocalAdaptiveLayoutInfo.current.navigationChromeType
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val bottomInset = navBarsPadding.calculateBottomPadding()

    return remember(chromeType, bottomInset, extraSpacing) {
        if (chromeType == NavigationChromeType.FloatingBottomBar) {
            FloatingBottomBarDefaults.Height + FloatingBottomBarDefaults.BottomMargin + extraSpacing + bottomInset
        } else {
            extraSpacing + bottomInset
        }
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
    val chromeType = LocalAdaptiveLayoutInfo.current.navigationChromeType
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val topBarInsetsPadding =
        WindowInsets.statusBars.union(WindowInsets.captionBar).asPaddingValues()
    val bottomInset = navBarsPadding.calculateBottomPadding()
    val topInset = topBarInsetsPadding.calculateTopPadding()

    return remember(
        chromeType,
        bottomInset,
        topInset,
        includeBottomBarPadding,
        extraBottomSpacing,
        extraTopSpacing,
        start,
        end
    ) {
        val isFloatingChrome = chromeType == NavigationChromeType.FloatingBottomBar
        PaddingValues(
            start = start,
            top = if (isFloatingChrome) {
                FloatingTopSearchBarDefaults.Height + (FloatingTopSearchBarDefaults.VerticalMargin * 2) + extraTopSpacing + topInset
            } else {
                extraTopSpacing
            },
            end = end,
            bottom = if (includeBottomBarPadding) {
                if (isFloatingChrome) {
                    FloatingBottomBarDefaults.Height + FloatingBottomBarDefaults.BottomMargin + extraBottomSpacing + bottomInset
                } else {
                    extraBottomSpacing + bottomInset
                }
            } else {
                0.dp
            }
        )
    }
}

