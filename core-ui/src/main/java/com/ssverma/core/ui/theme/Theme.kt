package com.ssverma.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.ssverma.core.ui.R

private val DarkColorPalette = darkColors(
    primary = darkGreen200,
    primaryVariant = darkGreen800,
    secondary = yellow200,
    background = gray900,
    surface = gray800,
    onPrimary = gray900,
    onSecondary = gray900,
    onBackground = Color.White,
    onSurface = white100,
)

private val LightColorPalette = lightColors(
    primary = blue600,
    primaryVariant = blue800,
    secondary = green600,
    background = Color.White,
    surface = white100,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = gray900,
    onSurface = gray900,
)

private val LightImages = Images(errorIllustrationResId = R.drawable.illustration_error_light)

private val DarkImages = Images(errorIllustrationResId = R.drawable.illustration_error_dark)

@Composable
fun ShowTimeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val images = if (darkTheme) DarkImages else LightImages

    CompositionLocalProvider(
        LocalImages provides images
    ) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}