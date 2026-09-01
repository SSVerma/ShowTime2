package com.ssverma.core.ads.quota

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class RewardPassType {
    AUTO_BACKUP,
    PRO_THEME,
    TRAKT_SYNC,
    EXTRA_CUSTOM_LIST,
    COMMUNITY_PUBLISH,
    CINEMA_GAME_REVIVE
}

data class RewardPassStatus(
    val isAutoBackupUnlocked: Boolean = false,
    val isProThemeUnlocked: Boolean = false,
    val isTraktSyncUnlocked: Boolean = false,
    val extraCustomListSlots: Int = 0,
    val extraCommunityPublishSlots: Int = 0,
    val cinemaGameRevivesRemaining: Int = 0
)

interface RewardManager {
    val passStatus: StateFlow<RewardPassStatus>

    suspend fun grantRewardPass(passType: RewardPassType)
    suspend fun canCreateCustomList(currentCount: Int, isProActive: Boolean): Boolean
    suspend fun canPublishCommunityList(currentActiveCount: Int, isProActive: Boolean): Boolean
    suspend fun isAutoBackupAllowed(isProActive: Boolean): Boolean
    suspend fun isThemeUnlocked(isProActive: Boolean): Boolean
    suspend fun isTraktSyncAllowed(isProActive: Boolean): Boolean
    suspend fun useCinemaGameRevive(): Boolean
}

@Singleton
class RewardManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appConfigProvider: AppConfigProvider,
    keyValueStorageClient: KeyValueStorageClient
) : RewardManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "reward_manager_prefs")
    )

    private val _passStatus = MutableStateFlow(RewardPassStatus())
    override val passStatus: StateFlow<RewardPassStatus> = _passStatus.asStateFlow()

    init {
        scope.launch {
            refreshPassStatus()
        }
    }

    private suspend fun refreshPassStatus() {
        val now = System.currentTimeMillis()
        val status = storage.data.map { prefs ->
            val autoBackupExpiry = prefs[KEY_AUTO_BACKUP_EXPIRY] ?: 0L
            val proThemeExpiry = prefs[KEY_PRO_THEME_EXPIRY] ?: 0L
            val traktSyncExpiry = prefs[KEY_TRAKT_SYNC_EXPIRY] ?: 0L
            val extraListSlots = prefs[KEY_EXTRA_LIST_SLOTS] ?: 0
            val extraPublishSlots = prefs[KEY_EXTRA_PUBLISH_SLOTS] ?: 0
            val revives = prefs[KEY_GAME_REVIVES] ?: 0

            RewardPassStatus(
                isAutoBackupUnlocked = autoBackupExpiry > now,
                isProThemeUnlocked = proThemeExpiry > now,
                isTraktSyncUnlocked = traktSyncExpiry > now,
                extraCustomListSlots = extraListSlots,
                extraCommunityPublishSlots = extraPublishSlots,
                cinemaGameRevivesRemaining = revives
            )
        }.first()
        _passStatus.value = status
    }

    override suspend fun grantRewardPass(passType: RewardPassType) {
        val now = System.currentTimeMillis()
        val backupDurationDays = appConfigProvider.getLong(KEY_CONFIG_REWARDED_BACKUP_DAYS, 7L)
        val themeDurationHours = appConfigProvider.getLong(KEY_CONFIG_REWARDED_THEME_HOURS, 24L)
        val traktDurationHours = appConfigProvider.getLong(KEY_CONFIG_REWARDED_TRAKT_HOURS, 24L)

        storage.edit { prefs ->
            when (passType) {
                RewardPassType.AUTO_BACKUP -> {
                    val currentExpiry = prefs[KEY_AUTO_BACKUP_EXPIRY] ?: 0L
                    val baseTime = if (currentExpiry > now) currentExpiry else now
                    prefs[KEY_AUTO_BACKUP_EXPIRY] =
                        baseTime + TimeUnit.DAYS.toMillis(backupDurationDays)
                }

                RewardPassType.PRO_THEME -> {
                    val currentExpiry = prefs[KEY_PRO_THEME_EXPIRY] ?: 0L
                    val baseTime = if (currentExpiry > now) currentExpiry else now
                    prefs[KEY_PRO_THEME_EXPIRY] =
                        baseTime + TimeUnit.HOURS.toMillis(themeDurationHours)
                }

                RewardPassType.TRAKT_SYNC -> {
                    val currentExpiry = prefs[KEY_TRAKT_SYNC_EXPIRY] ?: 0L
                    val baseTime = if (currentExpiry > now) currentExpiry else now
                    prefs[KEY_TRAKT_SYNC_EXPIRY] =
                        baseTime + TimeUnit.HOURS.toMillis(traktDurationHours)
                }

                RewardPassType.EXTRA_CUSTOM_LIST -> {
                    val current = prefs[KEY_EXTRA_LIST_SLOTS] ?: 0
                    prefs[KEY_EXTRA_LIST_SLOTS] = current + 1
                }

                RewardPassType.COMMUNITY_PUBLISH -> {
                    val current = prefs[KEY_EXTRA_PUBLISH_SLOTS] ?: 0
                    prefs[KEY_EXTRA_PUBLISH_SLOTS] = current + 1
                }

                RewardPassType.CINEMA_GAME_REVIVE -> {
                    val current = prefs[KEY_GAME_REVIVES] ?: 0
                    prefs[KEY_GAME_REVIVES] = current + 1
                }
            }
        }
        refreshPassStatus()
    }

    override suspend fun canCreateCustomList(currentCount: Int, isProActive: Boolean): Boolean {
        if (isProActive) return true
        val freeLimit = appConfigProvider.getLong(KEY_CONFIG_FREE_CUSTOM_LIST_LIMIT, 3L).toInt()
        val bonusSlots = _passStatus.value.extraCustomListSlots
        return currentCount < (freeLimit + bonusSlots)
    }

    override suspend fun canPublishCommunityList(
        currentActiveCount: Int,
        isProActive: Boolean
    ): Boolean {
        if (isProActive) return true
        val freeLimit = appConfigProvider.getLong(KEY_CONFIG_FREE_PUBLISH_LIMIT, 2L).toInt()
        val bonusSlots = _passStatus.value.extraCommunityPublishSlots
        return currentActiveCount < (freeLimit + bonusSlots)
    }

    override suspend fun isAutoBackupAllowed(isProActive: Boolean): Boolean {
        if (isProActive) return true
        val proRequired = appConfigProvider.getBoolean(KEY_CONFIG_AUTO_BACKUP_PRO_REQUIRED, true)
        if (!proRequired) return true
        return _passStatus.value.isAutoBackupUnlocked
    }

    override suspend fun isThemeUnlocked(isProActive: Boolean): Boolean {
        if (isProActive) return true
        return _passStatus.value.isProThemeUnlocked
    }

    override suspend fun isTraktSyncAllowed(isProActive: Boolean): Boolean {
        if (isProActive) return true
        val proRequired = appConfigProvider.getBoolean(KEY_CONFIG_TRAKT_SYNC_PRO_REQUIRED, true)
        if (!proRequired) return true
        return _passStatus.value.isTraktSyncUnlocked
    }

    override suspend fun useCinemaGameRevive(): Boolean {
        val current = _passStatus.value.cinemaGameRevivesRemaining
        if (current <= 0) return false
        storage.edit { prefs ->
            val slots = prefs[KEY_GAME_REVIVES] ?: 0
            if (slots > 0) prefs[KEY_GAME_REVIVES] = slots - 1
        }
        refreshPassStatus()
        return true
    }

    companion object {
        private val KEY_AUTO_BACKUP_EXPIRY = longPreferencesKey("reward_auto_backup_expiry")
        private val KEY_PRO_THEME_EXPIRY = longPreferencesKey("reward_pro_theme_expiry")
        private val KEY_TRAKT_SYNC_EXPIRY = longPreferencesKey("reward_trakt_sync_expiry")
        private val KEY_EXTRA_LIST_SLOTS = intPreferencesKey("reward_extra_list_slots")
        private val KEY_EXTRA_PUBLISH_SLOTS = intPreferencesKey("reward_extra_publish_slots")
        private val KEY_GAME_REVIVES = intPreferencesKey("reward_game_revives")

        const val KEY_CONFIG_FREE_CUSTOM_LIST_LIMIT = "free_custom_list_limit"
        const val KEY_CONFIG_FREE_PUBLISH_LIMIT = "free_community_publish_limit"
        const val KEY_CONFIG_REWARDED_BACKUP_DAYS = "rewarded_backup_duration_days"
        const val KEY_CONFIG_REWARDED_THEME_HOURS = "rewarded_theme_duration_hours"
        const val KEY_CONFIG_REWARDED_TRAKT_HOURS = "rewarded_trakt_duration_hours"
        const val KEY_CONFIG_AUTO_BACKUP_PRO_REQUIRED = "auto_backup_pro_required"
        const val KEY_CONFIG_TRAKT_SYNC_PRO_REQUIRED = "trakt_sync_pro_required"
    }
}
