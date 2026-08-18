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
    primary = Color(0xFFFF7A00),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF331500),
    onPrimaryContainer = Color(0xFFFFDBC7),

    secondary = Color(0xFF8E9196),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF141416),
    onSecondaryContainer = Color(0xFFE8EAED),

    tertiary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF000000),

    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),

    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF0D0E10),
    onSurfaceVariant = Color(0xFF8E9196),

    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF050507),
    surfaceContainer = Color(0xFF0A0B0E),
    surfaceContainerHigh = Color(0xFF121317),
    surfaceContainerHighest = Color(0xFF1B1C22),
    surfaceDim = Color(0xFF000000),
    surfaceBright = Color(0xFF22242B),

    outline = Color(0xFF3C3E44),
    outlineVariant = Color(0xFF1A1B20)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal val LightColorScheme = expressiveLightColorScheme().copy(
    primary = Color(0xFF1A73E8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8F0FE),
    onPrimaryContainer = Color(0xFF174EA6),

    secondary = Color(0xFF5F6368),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF1F3F4),
    onSecondaryContainer = Color(0xFF202124),

    tertiary = Color(0xFF202124),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFF6F8FA),
    onBackground = Color(0xFF202124),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF202124),
    surfaceVariant = Color(0xFFEFF2F6),
    onSurfaceVariant = Color(0xFF5F6368),

    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F4F7),
    surfaceContainer = Color(0xFFEBEFF4),
    surfaceContainerHigh = Color(0xFFE2E7ED),
    surfaceContainerHighest = Color(0xFFD8DFE7),
    surfaceDim = Color(0xFFD5DCE5),
    surfaceBright = Color(0xFFFAFBFD),

    outline = Color(0xFFC4C8D0),
    outlineVariant = Color(0xFFDCE0E6)
)
