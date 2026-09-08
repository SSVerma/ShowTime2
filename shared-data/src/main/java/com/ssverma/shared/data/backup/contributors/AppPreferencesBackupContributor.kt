package com.ssverma.shared.data.backup.contributors

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ssverma.core.backup.contributor.BackupContributor
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesBackupContributor @Inject constructor(
    private val appConfigRepository: AppConfigRepository
) : BackupContributor {

    private val gson = Gson()

    override val featureKey: String = FEATURE_KEY

    override suspend fun exportData(): JsonElement {
        val currentTheme = appConfigRepository.appTheme.firstOrNull() ?: AppTheme.System
        val currentRegion = appConfigRepository.watchProviderRegion.value

        val prefs = mapOf(
            KEY_PREF_THEME to currentTheme.name,
            KEY_PREF_REGION to currentRegion
        )
        return gson.toJsonTree(prefs)
    }

    override suspend fun importData(featurePayload: JsonElement?, fullSnapshot: JsonObject) {
        val prefsJson = featurePayload ?: fullSnapshot.get(FEATURE_KEY)
        if (prefsJson != null && !prefsJson.isJsonNull) {
            val type = object : TypeToken<Map<String, String>>() {}.type
            val prefs: Map<String, String>? = gson.fromJson(prefsJson, type)
            if (prefs != null) {
                prefs[KEY_PREF_THEME]?.let { themeName ->
                    val theme = AppTheme.fromName(themeName)
                    appConfigRepository.updateAppTheme(theme)
                }
                prefs[KEY_PREF_REGION]?.let { regionCode ->
                    if (regionCode.isNotBlank()) {
                        appConfigRepository.updateWatchProviderRegion(regionCode)
                    }
                }
            }
        }
    }

    override suspend fun getEntityCount(): Int {
        return 2
    }

    companion object {
        const val FEATURE_KEY = "preferences"
        const val KEY_PREF_THEME = "pref_theme"
        const val KEY_PREF_REGION = "pref_region"
    }
}
