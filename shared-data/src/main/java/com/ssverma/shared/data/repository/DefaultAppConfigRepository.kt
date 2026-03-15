package com.ssverma.shared.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.observe
import com.ssverma.core.storage.keyvalue.write
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.repository.AppConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DefaultAppConfigRepository @Inject constructor(
    @Named("app_config")
    private val keyValueStorage: KeyValueStorage
) : AppConfigRepository {

    override val appTheme: Flow<AppTheme>
        get() = keyValueStorage.observe(AppThemeKey, AppTheme.System.name).map {
            AppTheme.valueOf(it)
        }

    override val isDynamicColorEnabled: Flow<Boolean>
        get() = keyValueStorage.observe(DynamicColorKey, false)

    override suspend fun updateAppTheme(theme: AppTheme) {
        keyValueStorage.write(AppThemeKey, theme.name)
    }

    override suspend fun updateDynamicColor(enabled: Boolean) {
        keyValueStorage.write(DynamicColorKey, enabled)
    }

    override val isAppInfoBottomSheetDismissed: Flow<Boolean>
        get() = keyValueStorage.observe(AppInfoBottomSheetDismissedKey, false)

    override suspend fun dismissAppInfoBottomSheet() {
        keyValueStorage.write(AppInfoBottomSheetDismissedKey, true)
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val watchProviderRegion: StateFlow<String> = keyValueStorage.observe(
        key = WatchProviderRegionKey,
        default = Locale.getDefault().country.ifEmpty { "US" }
    ).distinctUntilChanged().stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = Locale.getDefault().country.ifEmpty { "US" }
    )

    override suspend fun updateWatchProviderRegion(regionCode: String) {
        keyValueStorage.write(WatchProviderRegionKey, regionCode)
    }

    override val isTranslationEnabled: StateFlow<Boolean> = keyValueStorage.observe(
        key = TranslationEnabledKey,
        default = false
    ).distinctUntilChanged().stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    override suspend fun updateTranslationEnabled(enabled: Boolean) {
        keyValueStorage.write(TranslationEnabledKey, enabled)
    }

    override val contentLanguage: StateFlow<String> = keyValueStorage.observe(
        key = ContentLanguageKey,
        default = Locale.getDefault().language.ifEmpty { "en" }
    ).distinctUntilChanged().stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = Locale.getDefault().language.ifEmpty { "en" }
    )

    override suspend fun updateContentLanguage(languageCode: String) {
        keyValueStorage.write(ContentLanguageKey, languageCode)
    }

    override val preferredOriginalLanguage: StateFlow<String> = keyValueStorage.observe(
        key = PreferredOriginalLanguageKey,
        default = ""
    ).distinctUntilChanged().stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )

    override suspend fun updatePreferredOriginalLanguage(languageCode: String) {
        keyValueStorage.write(PreferredOriginalLanguageKey, languageCode)
    }

    companion object {
        private val AppThemeKey = stringPreferencesKey("app_theme")
        private val DynamicColorKey = booleanPreferencesKey("dynamic_color")
        private val AppInfoBottomSheetDismissedKey =
            booleanPreferencesKey("app_info_bottom_sheet_dismissed")
        private val WatchProviderRegionKey = stringPreferencesKey("watch_provider_region")
        private val TranslationEnabledKey = booleanPreferencesKey("translation_enabled")
        private val ContentLanguageKey = stringPreferencesKey("content_language")
        private val PreferredOriginalLanguageKey = stringPreferencesKey("preferred_original_language")
    }
}
