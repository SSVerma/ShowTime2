package com.ssverma.core.ads.ui

import androidx.compose.runtime.compositionLocalOf
import com.ssverma.core.ads.config.AdConfigProvider

val LocalAdConfigProvider = compositionLocalOf<AdConfigProvider> {
    error("No AdConfigProvider provided")
}
