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

    background = Color(0xFF121212),
    onBackground = Color(0xFFE8EAED),

    surface = Color(0xFF121212),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF202124),
    onSurfaceVariant = Color(0xFF9AA0A6),

    outline = Color(0xFF5F6368)
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

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF202124),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF202124),
    surfaceVariant = Color(0xFFF8F9FA),
    onSurfaceVariant = Color(0xFF5F6368),

    outline = Color(0xFFDADCE0)
)
