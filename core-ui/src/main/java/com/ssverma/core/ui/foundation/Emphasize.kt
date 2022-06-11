package com.ssverma.core.ui.foundation

import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun Emphasize(
    contentAlpha: Float = ContentAlpha.medium,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
        content()
    }
}