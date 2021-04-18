package com.ssverma.showtime

import androidx.compose.runtime.Composable
import com.ssverma.showtime.ui.theme.ShowTimeTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@Composable
fun ShowTimeApp() {
    ShowTimeTheme {
        ProvideWindowInsets {
            NavGraph()
        }
    }
}