package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AppConfigRepository {
    val appTheme: Flow<AppTheme>

    val isDynamicColorEnabled: Flow<Boolean>

    val isAppInfoBottomSheetDismissed: Flow<Boolean>

    val watchProviderRegion: StateFlow<String>

    val isTranslationEnabled: StateFlow<Boolean>

    val contentLanguage: StateFlow<String>

    val preferredOriginalLanguage: StateFlow<String>

    val isAnalyticsEnabled: Flow<Boolean>

    val isNotificationsEnabled: Flow<Boolean>

    suspend fun updateAppTheme(theme: AppTheme)

    suspend fun updateDynamicColor(enabled: Boolean)

    suspend fun dismissAppInfoBottomSheet()

    suspend fun updateWatchProviderRegion(regionCode: String)

    suspend fun updateTranslationEnabled(enabled: Boolean)

    suspend fun updateContentLanguage(languageCode: String)

    suspend fun updatePreferredOriginalLanguage(languageCode: String)

    suspend fun updateAnalyticsEnabled(enabled: Boolean)

    suspend fun updateNotificationsEnabled(enabled: Boolean)
}
