package com.ssverma.shared.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.ssverma.core.di.AppScoped
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.repository.AppConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateHolder @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
    @AppScoped private val coroutineScope: CoroutineScope
) {
    val appTheme: StateFlow<AppTheme> = appConfigRepository.appTheme
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.System
        )

    val isDynamicColorEnabled: StateFlow<Boolean> = appConfigRepository.isDynamicColorEnabled
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isAppInfoDismissed: StateFlow<Boolean> = appConfigRepository.isAppInfoBottomSheetDismissed
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun updateAppTheme(theme: AppTheme) {
        coroutineScope.launch {
            appConfigRepository.updateAppTheme(theme)
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        coroutineScope.launch {
            appConfigRepository.updateDynamicColor(enabled)
        }
    }

    fun onDismissAppInfo(dontShowAgain: Boolean) {
        if (dontShowAgain) {
            coroutineScope.launch {
                appConfigRepository.dismissAppInfoBottomSheet()
            }
        }
    }
}

val LocalAppStateHolder = staticCompositionLocalOf<AppStateHolder> {
    error("No AppStateHolder provided")
}

val LocalAppInfoTrigger = staticCompositionLocalOf<() -> Unit> {
    { } // No-op default
}
