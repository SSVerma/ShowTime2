package com.ssverma.showtime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.ssverma.showtime.R

private val DarkColorPalette = darkColors(
    primary = yellow200,
    secondary = blue200,
    onSecondary = Color.Black,
    surface = yellowDark
)

private val LightColorPalette = lightColors(
    primary = red500,
    secondary = yellow200,
    primaryVariant = red600,
    onPrimary = Color.Black
)

private val LightImages = Images(errorIllustration = R.drawable.illustration_error_light)

private val DarkImages = Images(errorIllustration = R.drawable.illustration_error_dark)

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