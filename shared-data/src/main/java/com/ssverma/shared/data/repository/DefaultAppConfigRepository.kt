package com.ssverma.shared.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.observe
import com.ssverma.core.storage.keyvalue.write
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    companion object {
        private val AppThemeKey = stringPreferencesKey("app_theme")
        private val DynamicColorKey = booleanPreferencesKey("dynamic_color")
    }
}
