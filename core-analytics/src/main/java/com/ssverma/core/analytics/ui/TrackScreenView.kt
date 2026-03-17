package com.ssverma.core.analytics.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun TrackScreenView(screenName: String, screenClass: String? = null) {
    val analytics = LocalAnalytics.current

    DisposableEffect(screenName) {
        analytics.logScreenView(screenName = screenName, screenClass = screenClass)
        onDispose { }
    }
}
