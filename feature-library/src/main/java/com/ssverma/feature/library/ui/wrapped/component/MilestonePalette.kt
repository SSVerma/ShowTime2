package com.ssverma.feature.library.ui.wrapped.component

import androidx.compose.ui.graphics.Color
import com.ssverma.shared.domain.model.stats.MilestoneTier

object MilestonePalette {
    fun getTierColor(tier: MilestoneTier): Color = when (tier) {
        MilestoneTier.BRONZE -> Color(0xFFCD7F32)
        MilestoneTier.SILVER -> Color(0xFF9E9E9E)
        MilestoneTier.GOLD -> Color(0xFFFFB800)
        MilestoneTier.PLATINUM -> Color(0xFF00BCD4)
        MilestoneTier.DIAMOND -> Color(0xFF7C4DFF)
    }
}
