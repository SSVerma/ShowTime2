package com.ssverma.shared.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.ui.LocalAppStateHolder

@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier
) {
    val appStateHolder = LocalAppStateHolder.current
    val appTheme by appStateHolder.appTheme.collectAsState(initial = AppTheme.System)
    val isDynamicColorEnabled by appStateHolder.isDynamicColorEnabled.collectAsState(initial = false)

    AppThemeMenu(
        currentTheme = appTheme,
        onThemeSelected = { appStateHolder.updateAppTheme(it) },
        isDynamicColorEnabled = isDynamicColorEnabled,
        onDynamicColorToggled = { appStateHolder.updateDynamicColor(it) },
        modifier = modifier
    )
}
