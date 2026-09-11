package com.ssverma.feature.library.ui.share.component

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ssverma.feature.library.ui.share.ListShareColor
import com.ssverma.shared.domain.model.library.ListShareTheme

data class StoryCardThemeConfig(
    val brush: Brush,
    val border: Color,
    val accent: Color,
    val secondaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val cardBg: Color,
    val isVintage: Boolean = false
)

fun resolveCardTheme(theme: ListShareTheme): StoryCardThemeConfig {
    return when (theme) {
        ListShareTheme.CLASSIC_SHOWTIME -> StoryCardThemeConfig(
            brush = Brush.verticalGradient(
                listOf(ListShareColor.ClassicBackgroundStart, ListShareColor.ClassicBackgroundEnd)
            ),
            border = ListShareColor.ClassicAccent.copy(alpha = 0.35f),
            accent = ListShareColor.ClassicAccent,
            secondaryAccent = ListShareColor.ClassicAccent,
            textPrimary = ListShareColor.ClassicTextPrimary,
            textSecondary = ListShareColor.ClassicTextSecondary,
            cardBg = ListShareColor.ClassicCard
        )

        ListShareTheme.VINTAGE_35MM -> StoryCardThemeConfig(
            brush = Brush.verticalGradient(
                listOf(ListShareColor.VintageBackgroundStart, ListShareColor.VintageBackgroundEnd)
            ),
            border = ListShareColor.VintageAccent.copy(alpha = 0.4f),
            accent = ListShareColor.VintageAccent,
            secondaryAccent = ListShareColor.VintageAccent,
            textPrimary = ListShareColor.VintageTextPrimary,
            textSecondary = ListShareColor.VintageTextSecondary,
            cardBg = ListShareColor.VintageCard,
            isVintage = true
        )

        ListShareTheme.OLED_MIDNIGHT -> StoryCardThemeConfig(
            brush = Brush.verticalGradient(
                listOf(ListShareColor.OledBackgroundStart, ListShareColor.OledBackgroundEnd)
            ),
            border = ListShareColor.OledBorder,
            accent = ListShareColor.OledAccent,
            secondaryAccent = ListShareColor.OledAccent,
            textPrimary = ListShareColor.OledTextPrimary,
            textSecondary = ListShareColor.OledTextSecondary,
            cardBg = ListShareColor.OledCard
        )

        ListShareTheme.NEON_CYBERPUNK -> StoryCardThemeConfig(
            brush = Brush.verticalGradient(
                listOf(
                    ListShareColor.CyberpunkBackgroundStart,
                    ListShareColor.CyberpunkBackgroundEnd
                )
            ),
            border = ListShareColor.CyberpunkBorder,
            accent = ListShareColor.CyberpunkAccentPink,
            secondaryAccent = ListShareColor.CyberpunkAccentCyan,
            textPrimary = ListShareColor.CyberpunkTextPrimary,
            textSecondary = ListShareColor.CyberpunkTextSecondary,
            cardBg = ListShareColor.CyberpunkCard
        )
    }
}
