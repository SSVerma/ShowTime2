package com.ssverma.core.ui.foundation

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun Emphasize(
    alpha: Float = 0.6f,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = alpha)) {
        content()
    }
}