package com.ssverma.core.testing.fakes

import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAppConfigRepository(
    initialTheme: AppTheme = AppTheme.System,
    initialDynamicColor: Boolean = false,
    initialAppInfoDismissed: Boolean = false
) : AppConfigRepository {

    private val _appTheme = MutableStateFlow(initialTheme)
    override val appTheme: Flow<AppTheme> = _appTheme.asStateFlow()

    private val _isDynamicColorEnabled = MutableStateFlow(initialDynamicColor)
    override val isDynamicColorEnabled: Flow<Boolean> = _isDynamicColorEnabled.asStateFlow()

    private val _isAppInfoBottomSheetDismissed = MutableStateFlow(initialAppInfoDismissed)
    override val isAppInfoBottomSheetDismissed: Flow<Boolean> = _isAppInfoBottomSheetDismissed.asStateFlow()

    private val _watchProviderRegion = MutableStateFlow("US")
    override val watchProviderRegion: StateFlow<String> = _watchProviderRegion.asStateFlow()

    private val _isTranslationEnabled = MutableStateFlow(true)
    override val isTranslationEnabled: StateFlow<Boolean> = _isTranslationEnabled.asStateFlow()

    private val _contentLanguage = MutableStateFlow("en")
    override val contentLanguage: StateFlow<String> = _contentLanguage.asStateFlow()

    private val _preferredOriginalLanguage = MutableStateFlow("en")
    override val preferredOriginalLanguage: StateFlow<String> = _preferredOriginalLanguage.asStateFlow()

    private val _isAnalyticsEnabled = MutableStateFlow(true)
    override val isAnalyticsEnabled: Flow<Boolean> = _isAnalyticsEnabled.asStateFlow()

    private val _isNotificationsEnabled = MutableStateFlow(true)
    override val isNotificationsEnabled: Flow<Boolean> = _isNotificationsEnabled.asStateFlow()

    override suspend fun updateAppTheme(theme: AppTheme) {
        _appTheme.value = theme
    }

    override suspend fun updateDynamicColor(enabled: Boolean) {
        _isDynamicColorEnabled.value = enabled
    }

    override suspend fun dismissAppInfoBottomSheet() {
        _isAppInfoBottomSheetDismissed.value = true
    }

    override suspend fun updateWatchProviderRegion(regionCode: String) {
        _watchProviderRegion.value = regionCode
    }

    override suspend fun updateTranslationEnabled(enabled: Boolean) {
        _isTranslationEnabled.value = enabled
    }

    override suspend fun updateContentLanguage(languageCode: String) {
        _contentLanguage.value = languageCode
    }

    override suspend fun updatePreferredOriginalLanguage(languageCode: String) {
        _preferredOriginalLanguage.value = languageCode
    }

    override suspend fun updateAnalyticsEnabled(enabled: Boolean) {
        _isAnalyticsEnabled.value = enabled
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        _isNotificationsEnabled.value = enabled
    }
}
