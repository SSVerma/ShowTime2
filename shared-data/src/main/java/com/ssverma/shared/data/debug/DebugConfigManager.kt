package com.ssverma.shared.data.debug

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ssverma.core.billing.model.DebugProOverride
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.shared.domain.model.community.CommunityRating
import com.ssverma.shared.domain.model.community.DebugRatingOverride
import com.ssverma.shared.domain.model.community.RatingOverrideProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugConfigManager @Inject constructor(
    @ApplicationContext private val context: Context,
    keyValueStorageClient: KeyValueStorageClient
) : RatingOverrideProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "showtime_debug_prefs")
    )

    private companion object {
        val KEY_PRO_OVERRIDE = stringPreferencesKey("debug_pro_override")
        val KEY_MOCK_TRAKT = booleanPreferencesKey("debug_mock_trakt")
        val KEY_CUSTOM_TRAKT_CLIENT_ID = stringPreferencesKey("debug_custom_trakt_client_id")
        val KEY_DISABLE_ADS = booleanPreferencesKey("debug_disable_ads")
        val KEY_RATING_OVERRIDE = stringPreferencesKey("debug_rating_override")
        val KEY_RATING_THRESHOLD = intPreferencesKey("debug_rating_threshold")
    }

    val proOverride: StateFlow<DebugProOverride> = storage.data
        .map { prefs ->
            val value = prefs[KEY_PRO_OVERRIDE]
            try {
                if (value != null) DebugProOverride.valueOf(value) else DebugProOverride.AUTO
            } catch (_: Exception) {
                DebugProOverride.AUTO
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = DebugProOverride.AUTO
        )

    val isMockTraktEnabled: StateFlow<Boolean> = storage.data
        .map { prefs ->
            prefs[KEY_MOCK_TRAKT] ?: false
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val customTraktClientId: StateFlow<String> = storage.data
        .map { prefs ->
            prefs[KEY_CUSTOM_TRAKT_CLIENT_ID] ?: ""
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = ""
        )

    val isAdsDisabled: StateFlow<Boolean> = storage.data
        .map { prefs ->
            prefs[KEY_DISABLE_ADS] ?: false
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    override val ratingOverride: StateFlow<DebugRatingOverride> = storage.data
        .map { prefs ->
            val value = prefs[KEY_RATING_OVERRIDE]
            try {
                if (value != null) DebugRatingOverride.valueOf(value) else DebugRatingOverride.AUTO
            } catch (_: Exception) {
                DebugRatingOverride.AUTO
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = DebugRatingOverride.AUTO
        )

    override val ratingThreshold: StateFlow<Int> = storage.data
        .map { prefs ->
            prefs[KEY_RATING_THRESHOLD] ?: CommunityRating.MIN_SUFFICIENT_RATINGS_THRESHOLD
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = CommunityRating.MIN_SUFFICIENT_RATINGS_THRESHOLD
        )

    fun setProOverride(override: DebugProOverride) {
        scope.launch {
            storage.edit { prefs ->
                prefs[KEY_PRO_OVERRIDE] = override.name
            }
        }
    }

    fun setMockTraktEnabled(enabled: Boolean) {
        scope.launch {
            storage.edit { prefs ->
                prefs[KEY_MOCK_TRAKT] = enabled
            }
        }
    }

    fun setCustomTraktClientId(clientId: String) {
        scope.launch {
            storage.edit { prefs ->
                prefs[KEY_CUSTOM_TRAKT_CLIENT_ID] = clientId.trim()
            }
        }
    }

    fun setAdsDisabled(disabled: Boolean) {
        scope.launch {
            storage.edit { prefs ->
                prefs[KEY_DISABLE_ADS] = disabled
            }
        }
    }

    fun setRatingOverride(override: DebugRatingOverride) {
        scope.launch {
            storage.edit { prefs ->
                prefs[KEY_RATING_OVERRIDE] = override.name
            }
        }
    }

    fun setRatingThreshold(threshold: Int) {
        scope.launch {
            storage.edit { prefs ->
                prefs[KEY_RATING_THRESHOLD] = threshold
            }
        }
    }

    fun resetAll() {
        scope.launch {
            storage.edit { prefs ->
                prefs.clear()
            }
        }
    }
}
