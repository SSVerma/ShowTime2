package com.ssverma.shared.ui.discovery

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.ui.graphics.vector.ImageVector
import com.ssverma.shared.domain.model.discovery.DiscoveryVibePreset

val DiscoveryVibePreset.icon: ImageVector
    get() = when (this) {
        DiscoveryVibePreset.ALL -> Icons.Rounded.AutoAwesome
        DiscoveryVibePreset.MIND_BENDING -> Icons.Rounded.Psychology
        DiscoveryVibePreset.PURE_FUN -> Icons.Rounded.Celebration
        DiscoveryVibePreset.DARK_AND_GRITTY -> Icons.Rounded.Shield
        DiscoveryVibePreset.COMFORT_BINGE -> Icons.Rounded.Weekend
        DiscoveryVibePreset.EPIC_WORLDS -> Icons.Rounded.Public
        DiscoveryVibePreset.LATE_NIGHT_CHILLS -> Icons.Rounded.Bedtime
        DiscoveryVibePreset.MASTERPIECES -> Icons.Rounded.EmojiEvents
        DiscoveryVibePreset.QUICK_WATCH -> Icons.Rounded.HourglassBottom
    }
