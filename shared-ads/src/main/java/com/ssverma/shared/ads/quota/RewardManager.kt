package com.ssverma.shared.ads.quota

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.shared.ads.gate.FeaturePassPolicy
import com.ssverma.shared.domain.repository.ReminderQuotaManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generic, feature-agnostic reward pass engine.
 *
 * Manages timed passes and consumable slots keyed by [PassKey].
 * Contains zero knowledge of vertical feature implementations.
 */
interface RewardManager : ReminderQuotaManager {
    /** Observe whether a timed pass for [key] is currently active (unexpired). */
    fun isPassActive(key: PassKey): Flow<Boolean>

    /** Check whether a timed pass for [key] is currently active (unexpired) at this instant. */
    suspend fun isPassActiveNow(key: PassKey): Boolean

    /** Get the current expiration timestamp (in epoch ms) for [key], or 0 if inactive/expired. */
    fun getPassExpiryTimestamp(key: PassKey): Flow<Long>

    /**
     * Grants a timed pass for [key] lasting [durationMs].
     * If the pass is already active, extends the expiry timestamp.
     */
    suspend fun grantTimedPass(key: PassKey, durationMs: Long = TimeUnit.HOURS.toMillis(24))

    /** Observe extra consumable slots granted for [key]. */
    fun getExtraSlots(key: PassKey): Flow<Int>

    /** Get current count of extra consumable slots for [key]. */
    suspend fun getExtraSlotsCount(key: PassKey): Int

    /** Grants [slots] additional consumable slots for [key]. */
    suspend fun grantExtraSlots(key: PassKey, slots: Int = 1)

    /** Consumes 1 slot for [key] if available. Returns true if consumed, false if no slots available. */
    suspend fun consumeSlot(key: PassKey): Boolean

    /** Grants a reward based on a [FeaturePassPolicy]. */
    suspend fun grantPass(policy: FeaturePassPolicy) {
        when (policy) {
            is FeaturePassPolicy.TimedPass -> grantTimedPass(policy.passKey, policy.durationMs)
            is FeaturePassPolicy.ConsumableSlot -> grantExtraSlots(
                policy.passKey,
                policy.slotsGranted
            )

            is FeaturePassPolicy.ActionUnlock -> grantTimedPass(
                policy.passKey,
                TimeUnit.HOURS.toMillis(24)
            )
        }
    }

    /**
     * Generic quota check: determines if an action is allowed given the user's current count,
     * the free tier limit, and Pro subscription status.
     * Always returns true if [isProActive] is true. Otherwise, checks if currentCount < freeLimit + bonusSlots.
     */
    suspend fun canPerformQuotaAction(
        key: PassKey,
        currentCount: Int,
        freeLimit: Int,
        isProActive: Boolean
    ): Boolean {
        if (isProActive) return true
        val bonusSlots = getExtraSlotsCount(key)
        return currentCount < (freeLimit + bonusSlots)
    }

    /**
     * Generic feature access check: returns true if Pro is active, or if Pro is not required,
     * or if an active pass exists for [key].
     */
    suspend fun isFeatureAllowed(
        key: PassKey,
        isProActive: Boolean,
        isProRequired: Boolean = true
    ): Boolean {
        if (isProActive || !isProRequired) return true
        return isPassActiveNow(key)
    }
}

@Singleton
class RewardManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appConfigProvider: AppConfigProvider,
    keyValueStorageClient: KeyValueStorageClient
) : RewardManager, ReminderQuotaManager {

    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "reward_manager_prefs")
    )

    override fun isPassActive(key: PassKey): Flow<Boolean> {
        val expiryKey = key.toExpiryKey()
        return storage.data.map { prefs ->
            val expiry = prefs[expiryKey] ?: 0L
            expiry > System.currentTimeMillis()
        }.distinctUntilChanged()
    }

    override suspend fun isPassActiveNow(key: PassKey): Boolean {
        val expiryKey = key.toExpiryKey()
        val expiry = storage.data.map { it[expiryKey] ?: 0L }.first()
        return expiry > System.currentTimeMillis()
    }

    override fun getPassExpiryTimestamp(key: PassKey): Flow<Long> {
        val expiryKey = key.toExpiryKey()
        return storage.data.map { prefs ->
            val expiry = prefs[expiryKey] ?: 0L
            if (expiry > System.currentTimeMillis()) expiry else 0L
        }.distinctUntilChanged()
    }

    override suspend fun grantTimedPass(key: PassKey, durationMs: Long) {
        val now = System.currentTimeMillis()
        val expiryKey = key.toExpiryKey()
        storage.edit { prefs ->
            val currentExpiry = prefs[expiryKey] ?: 0L
            val baseTime = if (currentExpiry > now) currentExpiry else now
            prefs[expiryKey] = baseTime + durationMs
        }
    }

    override fun getExtraSlots(key: PassKey): Flow<Int> {
        val slotsKey = key.toSlotsKey()
        return storage.data.map { prefs ->
            prefs[slotsKey] ?: 0
        }.distinctUntilChanged()
    }

    override suspend fun getExtraSlotsCount(key: PassKey): Int {
        val slotsKey = key.toSlotsKey()
        return storage.data.map { it[slotsKey] ?: 0 }.first()
    }

    override suspend fun grantExtraSlots(key: PassKey, slots: Int) {
        val slotsKey = key.toSlotsKey()
        storage.edit { prefs ->
            val current = prefs[slotsKey] ?: 0
            prefs[slotsKey] = current + slots
        }
    }

    override suspend fun consumeSlot(key: PassKey): Boolean {
        val slotsKey = key.toSlotsKey()
        var consumed = false
        storage.edit { prefs ->
            val current = prefs[slotsKey] ?: 0
            if (current > 0) {
                prefs[slotsKey] = current - 1
                consumed = true
            }
        }
        return consumed
    }

    // --- ReminderQuotaManager Implementation ---

    override suspend fun canScheduleReminder(
        currentActiveCount: Int,
        isProActive: Boolean
    ): Boolean {
        val freeLimit = appConfigProvider.getLong(KEY_CONFIG_FREE_REMINDERS_LIMIT, 3L).toInt()
        return canPerformQuotaAction(
            AiringReminderPassKey,
            currentActiveCount,
            freeLimit,
            isProActive
        )
    }

    override suspend fun grantReminderPass() {
        grantExtraSlots(AiringReminderPassKey, 1)
    }

    override suspend fun consumeReminderPass(): Boolean {
        return consumeSlot(AiringReminderPassKey)
    }

    companion object {
        val AiringReminderPassKey = PassKey("airing_reminders")

        private fun PassKey.toExpiryKey(): Preferences.Key<Long> =
            longPreferencesKey("pass_expiry_$value")

        private fun PassKey.toSlotsKey(): Preferences.Key<Int> =
            intPreferencesKey("pass_slots_$value")

        const val KEY_CONFIG_FREE_REMINDERS_LIMIT = "config_free_reminders_limit"
    }
}
