package com.ssverma.core.notifications

import androidx.compose.runtime.staticCompositionLocalOf

val LocalNotificationManager = staticCompositionLocalOf<ShowTimeNotificationManager> {
    error("No ShowTimeNotificationManager provided")
}
