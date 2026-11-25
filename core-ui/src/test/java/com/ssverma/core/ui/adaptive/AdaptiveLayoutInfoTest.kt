package com.ssverma.core.ui.adaptive

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdaptiveLayoutInfoTest {

    @Test
    fun compactWidths_resolveToFloatingBottomBarAndSinglePane() {
        listOf(360.dp, 412.dp, 599.dp).forEach { widthDp ->
            val widthClass = calculateShowTimeWidthClass(windowWidthDp = widthDp)
            val info = AdaptiveLayoutInfo(
                widthClass = widthClass,
                windowWidthDp = widthDp,
                windowHeightDp = 800.dp
            )

            assertThat(info.widthClass).isEqualTo(ShowTimeWidthClass.Compact)
            assertThat(info.navigationChromeType).isEqualTo(NavigationChromeType.FloatingBottomBar)
            assertThat(info.useFloatingModalSheets).isFalse()
            assertThat(info.isMultiPaneEligible).isFalse()
        }
    }

    @Test
    fun mediumWidthsBelowMultiPaneThreshold_resolveToCompactRailAndFloatingModal() {
        listOf(600.dp, 720.dp, 839.dp).forEach { widthDp ->
            val widthClass = calculateShowTimeWidthClass(windowWidthDp = widthDp)
            val info = AdaptiveLayoutInfo(
                widthClass = widthClass,
                windowWidthDp = widthDp,
                windowHeightDp = 900.dp
            )

            assertThat(info.widthClass).isEqualTo(ShowTimeWidthClass.Medium)
            assertThat(info.navigationChromeType).isEqualTo(NavigationChromeType.CompactRail)
            assertThat(info.useFloatingModalSheets).isTrue()
            assertThat(info.isMultiPaneEligible).isFalse()
        }
    }

    @Test
    fun mediumWidthsAtOrAboveMultiPaneThreshold_enableMultiPaneAndCompactRail() {
        listOf(840.dp, 1024.dp, 1199.dp).forEach { widthDp ->
            val widthClass = calculateShowTimeWidthClass(windowWidthDp = widthDp)
            val info = AdaptiveLayoutInfo(
                widthClass = widthClass,
                windowWidthDp = widthDp,
                windowHeightDp = 800.dp
            )

            assertThat(info.widthClass).isEqualTo(ShowTimeWidthClass.Medium)
            assertThat(info.navigationChromeType).isEqualTo(NavigationChromeType.CompactRail)
            assertThat(info.useFloatingModalSheets).isTrue()
            assertThat(info.isMultiPaneEligible).isTrue()
        }
    }

    @Test
    fun largeWidths_resolveToPermanentSidebarAndMultiPane() {
        listOf(1200.dp, 1440.dp, 1920.dp).forEach { widthDp ->
            val widthClass = calculateShowTimeWidthClass(windowWidthDp = widthDp)
            val info = AdaptiveLayoutInfo(
                widthClass = widthClass,
                windowWidthDp = widthDp,
                windowHeightDp = 900.dp
            )

            assertThat(info.widthClass).isEqualTo(ShowTimeWidthClass.Large)
            assertThat(info.navigationChromeType).isEqualTo(NavigationChromeType.PermanentSidebar)
            assertThat(info.useFloatingModalSheets).isTrue()
            assertThat(info.isMultiPaneEligible).isTrue()
        }
    }
}
