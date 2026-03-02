package com.ssverma.core.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.ssverma.core.ui.R

@Immutable
data class Images(
    @DrawableRes val errorIllustrationResId: Int
)

val LocalImages = staticCompositionLocalOf<Images> {
    error("No images provided")
}

internal val LightImages = Images(
    errorIllustrationResId = R.drawable.illustration_error_light
)

internal val DarkImages = Images(
    errorIllustrationResId = R.drawable.illustration_error_dark
)
