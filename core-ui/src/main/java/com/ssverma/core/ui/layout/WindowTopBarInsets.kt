package com.ssverma.core.ui.layout

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Combines status bar and desktop/freeform caption bar top insets so custom headers and
 * persistent chrome remain clear of window controls across mobile, tablet, and desktop windows.
 */
@Composable
fun rememberWindowTopBarInsets(): WindowInsets {
    return WindowInsets.statusBars
        .union(WindowInsets.captionBar)
        .only(WindowInsetsSides.Top)
}

/**
 * Applies combined top padding for both status bars and freeform desktop caption bars.
 */
fun Modifier.windowTopBarInsetsPadding(): Modifier = composed {
    windowInsetsPadding(rememberWindowTopBarInsets())
}
