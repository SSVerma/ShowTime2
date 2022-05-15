package com.ssverma.core.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class Images(
    @DrawableRes val errorIllustrationResId: Int
)

internal val LocalImages = staticCompositionLocalOf<Images> {
    error("No LocalImages specified")
}