package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AppConfigRepository {
    val appTheme: Flow<AppTheme>

    val isDynamicColorEnabled: Flow<Boolean>

    suspend fun updateAppTheme(theme: AppTheme)

    suspend fun updateDynamicColor(enabled: Boolean)

    val isAppInfoBottomSheetDismissed: Flow<Boolean>

    suspend fun dismissAppInfoBottomSheet()

    val watchProviderRegion: StateFlow<String>

    suspend fun updateWatchProviderRegion(regionCode: String)
}
