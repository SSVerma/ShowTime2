package com.ssverma.shared.testing.compose

import androidx.compose.runtime.Composable
import com.ssverma.core.ui.theme.ShowTimeTheme
import com.ssverma.shared.domain.model.AppTheme

@Composable
fun ShowTimeTestTheme(
    appTheme: AppTheme = AppTheme.Dark,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ShowTimeTheme(
        appTheme = appTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
