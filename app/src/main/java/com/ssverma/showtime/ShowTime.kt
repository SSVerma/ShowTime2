package com.ssverma.showtime

import androidx.compose.runtime.Composable
import com.google.accompanist.insets.ProvideWindowInsets
import com.ssverma.showtime.ui.theme.ShowTimeTheme

@Composable
fun ShowTime() {
    ShowTimeTheme {
        ProvideWindowInsets {
            NavGraph()
        }
    }
}