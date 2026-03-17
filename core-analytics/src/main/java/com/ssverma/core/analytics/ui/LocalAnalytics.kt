package com.ssverma.core.analytics.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.ssverma.core.analytics.Analytics

val LocalAnalytics = staticCompositionLocalOf<Analytics> {
    error("No Analytics provided")
}
