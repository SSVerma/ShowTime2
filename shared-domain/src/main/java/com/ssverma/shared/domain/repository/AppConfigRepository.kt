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

    val isTranslationEnabled: StateFlow<Boolean>
    suspend fun updateTranslationEnabled(enabled: Boolean)

    val contentLanguage: StateFlow<String>
    suspend fun updateContentLanguage(languageCode: String)

    val preferredOriginalLanguage: StateFlow<String>
    suspend fun updatePreferredOriginalLanguage(languageCode: String)
}
