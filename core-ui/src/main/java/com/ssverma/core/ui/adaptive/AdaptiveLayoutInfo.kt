package com.ssverma.core.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified

/**
 * Window width breakpoints separating compact phones, medium tablets/foldables,
 * and large desktop/Googlebook viewports.
 */
@Immutable
enum class ShowTimeWidthClass {
    Compact,
    Medium,
    Large
}

/**
 * Top-level navigation chrome rendered by the application shell.
 */
@Immutable
enum class NavigationChromeType {
    FloatingBottomBar,
    CompactRail,
    PermanentSidebar
}

/**
 * Responsive window layout metrics provided down the composition tree via [LocalAdaptiveLayoutInfo].
 */
@Immutable
data class AdaptiveLayoutInfo(
    val widthClass: ShowTimeWidthClass = ShowTimeWidthClass.Compact,
    val windowWidthDp: Dp = AdaptiveBreakpointDefaults.DefaultCompactWidth,
    val windowHeightDp: Dp = AdaptiveBreakpointDefaults.DefaultCompactHeight
) {
    val navigationChromeType: NavigationChromeType
        get() = when (widthClass) {
            ShowTimeWidthClass.Compact -> NavigationChromeType.FloatingBottomBar
            ShowTimeWidthClass.Medium -> NavigationChromeType.CompactRail
            ShowTimeWidthClass.Large -> NavigationChromeType.PermanentSidebar
        }

    val useFloatingModalSheets: Boolean
        get() = widthClass != ShowTimeWidthClass.Compact

    val isMultiPaneEligible: Boolean
        get() = windowWidthDp >= AdaptiveBreakpointDefaults.MultiPaneMinWidth
}

object AdaptiveBreakpointDefaults {
    val MediumMinWidth: Dp = 600.dp
    val MultiPaneMinWidth: Dp = 840.dp
    val LargeMinWidth: Dp = 1200.dp
    val DefaultCompactWidth: Dp = 360.dp
    val DefaultCompactHeight: Dp = 800.dp
}

/**
 * Resolves [ShowTimeWidthClass] from an arbitrary window width in Dp.
 */
fun calculateShowTimeWidthClass(windowWidthDp: Dp): ShowTimeWidthClass {
    return when {
        windowWidthDp >= AdaptiveBreakpointDefaults.LargeMinWidth -> ShowTimeWidthClass.Large
        windowWidthDp >= AdaptiveBreakpointDefaults.MediumMinWidth -> ShowTimeWidthClass.Medium
        else -> ShowTimeWidthClass.Compact
    }
}

val LocalAdaptiveLayoutInfo = compositionLocalOf { AdaptiveLayoutInfo() }

/**
 * Observes live container window dimensions so free-form window resizes update [AdaptiveLayoutInfo]
 * without relying on physical screen size or orientation locks.
 */
@Composable
fun rememberAdaptiveLayoutInfo(): AdaptiveLayoutInfo {
    val windowInfo = LocalWindowInfo.current
    val containerDpSize = windowInfo.containerDpSize
    val containerPxSize = windowInfo.containerSize
    val density = LocalDensity.current

    val resolvedWidthDp = when {
        containerDpSize.isSpecified && containerDpSize.width > 0.dp -> containerDpSize.width
        containerPxSize != IntSize.Zero && containerPxSize.width > 0 -> {
            with(density) { containerPxSize.width.toDp() }
        }

        else -> AdaptiveBreakpointDefaults.DefaultCompactWidth
    }
    val resolvedHeightDp = when {
        containerDpSize.isSpecified && containerDpSize.height > 0.dp -> containerDpSize.height
        containerPxSize != IntSize.Zero && containerPxSize.height > 0 -> {
            with(density) { containerPxSize.height.toDp() }
        }

        else -> AdaptiveBreakpointDefaults.DefaultCompactHeight
    }

    return remember(resolvedWidthDp, resolvedHeightDp) {
        AdaptiveLayoutInfo(
            widthClass = calculateShowTimeWidthClass(windowWidthDp = resolvedWidthDp),
            windowWidthDp = resolvedWidthDp,
            windowHeightDp = resolvedHeightDp
        )
    }
}
