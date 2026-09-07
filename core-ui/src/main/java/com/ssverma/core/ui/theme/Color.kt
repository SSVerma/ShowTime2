package com.ssverma.core.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.ui.graphics.Color

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF7A00),
    onPrimary = Color(0xFF3D1900),
    primaryContainer = Color(0xFF662A00),
    onPrimaryContainer = Color(0xFFFFDBC7),

    secondary = Color(0xFF9AA0A6),
    onSecondary = Color(0xFF121212),
    secondaryContainer = Color(0xFF28292A),
    onSecondaryContainer = Color(0xFFE8EAED),

    tertiary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF000000),

    background = Color(0xFF0D0E11),
    onBackground = Color(0xFFE8EAED),

    surface = Color(0xFF15161A),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF202226),
    onSurfaceVariant = Color(0xFF9AA0A6),

    surfaceContainerLowest = Color(0xFF0D0E11),
    surfaceContainerLow = Color(0xFF191A1F),
    surfaceContainer = Color(0xFF1F2026),
    surfaceContainerHigh = Color(0xFF272930),
    surfaceContainerHighest = Color(0xFF32343C),
    surfaceDim = Color(0xFF111215),
    surfaceBright = Color(0xFF3B3D46),

    outline = Color(0xFF5F6368),
    outlineVariant = Color(0xFF2B2C2F)
)

internal val OledMidnightColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1E1E1E),
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF181818),
    onSecondaryContainer = Color(0xFFEEEEEE),

    tertiary = Color(0xFFE0E0E0),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF242424),
    onTertiaryContainer = Color(0xFFFFFFFF),

    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),

    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF0D0D0D),
    onSurfaceVariant = Color(0xFF9E9E9E),

    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF050505),
    surfaceContainer = Color(0xFF0A0A0A),
    surfaceContainerHigh = Color(0xFF141414),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    surfaceDim = Color(0xFF000000),
    surfaceBright = Color(0xFF282828),

    outline = Color(0xFF2C2C2C),
    outlineVariant = Color(0xFF1A1A1A)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal val LightColorScheme = expressiveLightColorScheme().copy(
    primary = Color(0xFF1A73E8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD2E3FC),
    onPrimaryContainer = Color(0xFF174EA6),

    secondary = Color(0xFF5F6368),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8F0FE),
    onSecondaryContainer = Color(0xFF1967D2),

    tertiary = Color(0xFF202124),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF202124),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF202124),
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF5F6368),

    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F9FA),
    surfaceContainer = Color(0xFFF1F3F4),
    surfaceContainerHigh = Color(0xFFE8EAED),
    surfaceContainerHighest = Color(0xFFDADCE0),
    surfaceDim = Color(0xFFD8DCE4),
    surfaceBright = Color(0xFFFFFFFF),

    outline = Color(0xFF747775),
    outlineVariant = Color(0xFFC4C7C5)
)
