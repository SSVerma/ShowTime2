package com.ssverma.core.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.ui.graphics.Color

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF27272A),
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFFD4D4D8),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF18181B),
    onSecondaryContainer = Color(0xFFF4F4F5),

    tertiary = Color(0xFF3B82F6),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFF000000),
    onBackground = Color(0xFFF4F4F5),

    surface = Color(0xFF000000),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF18181B),
    onSurfaceVariant = Color(0xFFA1A1AA),

    outline = Color(0xFF27272A)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal val LightColorScheme = expressiveLightColorScheme().copy(
    primary = Color(0xFF09090B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF4F4F5),
    onPrimaryContainer = Color(0xFF09090B),

    secondary = Color(0xFF52525B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE4E4E7),
    onSecondaryContainer = Color(0xFF18181B),

    tertiary = Color(0xFF2563EB),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF09090B),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF09090B),
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = Color(0xFF52525B),

    outline = Color(0xFFE4E4E7)
)
