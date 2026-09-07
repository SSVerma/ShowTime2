package com.ssverma.shared.domain.model.library

enum class ListShareTheme(val isProOnly: Boolean) {
    CLASSIC_SHOWTIME(isProOnly = false),
    VINTAGE_35MM(isProOnly = true),
    OLED_MIDNIGHT(isProOnly = true),
    NEON_CYBERPUNK(isProOnly = true)
}

enum class ListShareCardFormat {
    STORY_9_16,
    SQUARE_1_1
}
