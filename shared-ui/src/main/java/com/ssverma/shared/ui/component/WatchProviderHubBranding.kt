package com.ssverma.shared.ui.component

import androidx.compose.ui.graphics.Color

object WatchProviderHubBranding {
    fun getBrandingColor(providerId: Int): Color {
        return when (providerId) {
            8 -> Color(0xFFE50914) // Netflix
            119 -> Color(0xFF00A8E1) // Amazon Prime
            122 -> Color(0xFF004E96) // Hotstar
            337 -> Color(0xFF131921) // Disney+
            232 -> Color(0xFFFFFFFF) // Zee5 (Mainly white/dynamic)
            121 -> Color(0xFFFF4B00) // Voot
            else -> Color.DarkGray
        }
    }
}
