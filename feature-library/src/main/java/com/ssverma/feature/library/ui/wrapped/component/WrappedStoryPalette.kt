package com.ssverma.feature.library.ui.wrapped.component

import androidx.compose.ui.graphics.Color

enum class WrappedStoryStyle(val isProOnly: Boolean) {
    CLASSIC_VELVET(isProOnly = false),
    OLED_NOIR(isProOnly = true),
    NEON_CYBERPUNK(isProOnly = true),
    GOLDEN_VIP(isProOnly = true)
}

object WrappedStoryPalette {
    // Classic Velvet
    val VelvetTop = Color(0xFF1E0F26)
    val VelvetBottom = Color(0xFF0C0611)
    val VelvetAccent = Color(0xFFFF6584)
    val VelvetAccentSecondary = Color(0xFFB388FF)
    val VelvetTextWhite = Color(0xFFFFFFFF)
    val VelvetTextMuted = Color(0xFFB39DDB)
    val VelvetCardBg = Color(0xFF2B1638)

    // OLED Noir
    val OledTop = Color(0xFF000000)
    val OledBottom = Color(0xFF080808)
    val OledAccent = Color(0xFFFFFFFF)
    val OledBorder = Color(0xFF2C2C2C)
    val OledTextWhite = Color(0xFFFFFFFF)
    val OledTextMuted = Color(0xFF888888)
    val OledCardBg = Color(0xFF121212)

    // Neon Cyberpunk
    val CyberpunkTop = Color(0xFF080D1E)
    val CyberpunkBottom = Color(0xFF03050D)
    val CyberpunkCyan = Color(0xFF00E5FF)
    val CyberpunkMagenta = Color(0xFFFF007F)
    val CyberpunkTextWhite = Color(0xFFE1F5FE)
    val CyberpunkTextMuted = Color(0xFF7986CB)
    val CyberpunkCardBg = Color(0xFF0F172E)

    // Golden VIP
    val GoldTop = Color(0xFF261D0C)
    val GoldBottom = Color(0xFF0E0B05)
    val GoldAccent = Color(0xFFFFD54F)
    val GoldLight = Color(0xFFFFF8E1)
    val GoldMuted = Color(0xFFC5A859)
    val GoldTextWhite = Color(0xFFFFFDE7)
    val GoldTextMuted = Color(0xFFD7CCC8)
    val GoldCardBg = Color(0xFF332712)
}
