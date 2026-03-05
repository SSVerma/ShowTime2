package com.ssverma.core.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.ui.graphics.Color

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF0000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF410002),
    onPrimaryContainer = Color(0xFFFFDAD6),

    secondary = Color(0xFFF1F1F1),
    onSecondary = Color(0xFF0F0F0F),
    secondaryContainer = Color(0xFF272727),
    onSecondaryContainer = Color(0xFFF1F1F1),

    tertiary = Color(0xFF3EA6FF),
    onTertiary = Color(0xFF00325A),

    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFF1F1F1),

    surface = Color(0xFF0F0F0F),
    onSurface = Color(0xFFF1F1F1),
    surfaceVariant = Color(0xFF212121),
    onSurfaceVariant = Color(0xFFAAAAAA),

    outline = Color(0xFF3F3F3F)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal val LightColorScheme = expressiveLightColorScheme().copy(
    primary = Color(0xFF0A192F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD2E4FF),
    onPrimaryContainer = Color(0xFF001C37),

    secondary = Color(0xFF9E7C00),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDF94),
    onSecondaryContainer = Color(0xFF251A00),

    tertiary = Color(0xFF5A5D72),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF8F9FA),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2E8),
    onSurfaceVariant = Color(0xFF44474E)
)
