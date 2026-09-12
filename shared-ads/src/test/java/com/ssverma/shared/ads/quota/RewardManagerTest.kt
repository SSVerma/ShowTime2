package com.ssverma.shared.ads.quota

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.testing.fakes.FakeAppConfigProvider
import com.ssverma.shared.ads.gate.FeaturePassPolicy
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RewardManagerTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val fakeAppConfigProvider = FakeAppConfigProvider()
    private val mockKeyValueStorageClient: KeyValueStorageClient = mockk(relaxed = true)
    private val mockStorage: KeyValueStorage = mockk(relaxed = true)
    private val preferencesFlow = MutableStateFlow(emptyPreferences())

    private lateinit var rewardManager: RewardManagerImpl

    private val testPassKey = PassKey("test_pass")
    private val testSlotKey = PassKey("test_slots")

    @Before
    fun setUp() {
        preferencesFlow.value = emptyPreferences()
        every { mockKeyValueStorageClient.createKeyValueStorage(any(), any()) } returns mockStorage
        every { mockStorage.data } returns preferencesFlow
        coEvery { mockStorage.updateData(any()) } coAnswers {
            val transform =
                firstArg<suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences>()
            val updated = transform(preferencesFlow.value)
            preferencesFlow.value = updated
            updated
        }

        rewardManager = RewardManagerImpl(
            context = mockContext,
            appConfigProvider = fakeAppConfigProvider,
            keyValueStorageClient = mockKeyValueStorageClient
        )
    }

    @Test
    fun `isPassActive returns false when pass has not been granted`() = runTest {
        val isActive = rewardManager.isPassActive(testPassKey).first()
        assertThat(isActive).isFalse()
        assertThat(rewardManager.isPassActiveNow(testPassKey)).isFalse()
    }

    @Test
    fun `grantTimedPass activates pass and sets expiry timestamp`() = runTest {
        val durationMs = TimeUnit.HOURS.toMillis(24)
        val before = System.currentTimeMillis()

        rewardManager.grantTimedPass(testPassKey, durationMs)

        assertThat(rewardManager.isPassActive(testPassKey).first()).isTrue()
        assertThat(rewardManager.isPassActiveNow(testPassKey)).isTrue()

        val expiry = rewardManager.getPassExpiryTimestamp(testPassKey).first()
        assertThat(expiry).isAtLeast(before + durationMs)
    }

    @Test
    fun `grantTimedPass extends expiry if pass already active`() = runTest {
        val firstDuration = TimeUnit.HOURS.toMillis(12)
        val extension = TimeUnit.HOURS.toMillis(24)

        rewardManager.grantTimedPass(testPassKey, firstDuration)
        val initialExpiry = rewardManager.getPassExpiryTimestamp(testPassKey).first()

        rewardManager.grantTimedPass(testPassKey, extension)
        val extendedExpiry = rewardManager.getPassExpiryTimestamp(testPassKey).first()

        assertThat(extendedExpiry).isEqualTo(initialExpiry + extension)
    }

    @Test
    fun `getExtraSlots returns 0 initially`() = runTest {
        assertThat(rewardManager.getExtraSlotsCount(testSlotKey)).isEqualTo(0)
        assertThat(rewardManager.getExtraSlots(testSlotKey).first()).isEqualTo(0)
    }

    @Test
    fun `grantExtraSlots increments available slot count`() = runTest {
        rewardManager.grantExtraSlots(testSlotKey, 2)
        assertThat(rewardManager.getExtraSlotsCount(testSlotKey)).isEqualTo(2)

        rewardManager.grantExtraSlots(testSlotKey, 1)
        assertThat(rewardManager.getExtraSlotsCount(testSlotKey)).isEqualTo(3)
        assertThat(rewardManager.getExtraSlots(testSlotKey).first()).isEqualTo(3)
    }

    @Test
    fun `consumeSlot decrements slots and returns true when available`() = runTest {
        rewardManager.grantExtraSlots(testSlotKey, 1)

        val consumed = rewardManager.consumeSlot(testSlotKey)
        assertThat(consumed).isTrue()
        assertThat(rewardManager.getExtraSlotsCount(testSlotKey)).isEqualTo(0)

        val consumedAgain = rewardManager.consumeSlot(testSlotKey)
        assertThat(consumedAgain).isFalse()
    }

    @Test
    fun `grantPass with TimedPass policy activates timed pass`() = runTest {
        val policy = FeaturePassPolicy.TimedPass(
            passKey = testPassKey,
            durationMs = TimeUnit.HOURS.toMillis(48)
        )
        rewardManager.grantPass(policy)

        assertThat(rewardManager.isPassActiveNow(testPassKey)).isTrue()
    }

    @Test
    fun `grantPass with ConsumableSlot policy grants slots`() = runTest {
        val policy = FeaturePassPolicy.ConsumableSlot(
            passKey = testSlotKey,
            slotsGranted = 3
        )
        rewardManager.grantPass(policy)

        assertThat(rewardManager.getExtraSlotsCount(testSlotKey)).isEqualTo(3)
    }

    @Test
    fun `grantPass with ActionUnlock policy activates 24h pass`() = runTest {
        val policy = FeaturePassPolicy.ActionUnlock(passKey = testPassKey)
        rewardManager.grantPass(policy)

        assertThat(rewardManager.isPassActiveNow(testPassKey)).isTrue()
    }

    @Test
    fun `canPerformQuotaAction returns true for Pro user regardless of count`() = runTest {
        val allowed = rewardManager.canPerformQuotaAction(
            key = testSlotKey,
            currentCount = 100,
            freeLimit = 3,
            isProActive = true
        )
        assertThat(allowed).isTrue()
    }

    @Test
    fun `canPerformQuotaAction respects free limit plus bonus slots for free user`() = runTest {
        val allowedUnder = rewardManager.canPerformQuotaAction(
            key = testSlotKey,
            currentCount = 2,
            freeLimit = 3,
            isProActive = false
        )
        assertThat(allowedUnder).isTrue()

        val allowedAtLimit = rewardManager.canPerformQuotaAction(
            key = testSlotKey,
            currentCount = 3,
            freeLimit = 3,
            isProActive = false
        )
        assertThat(allowedAtLimit).isFalse()

        // Grant 2 bonus slots
        rewardManager.grantExtraSlots(testSlotKey, 2)

        val allowedWithBonus = rewardManager.canPerformQuotaAction(
            key = testSlotKey,
            currentCount = 4,
            freeLimit = 3,
            isProActive = false
        )
        assertThat(allowedWithBonus).isTrue()

        val allowedAtNewLimit = rewardManager.canPerformQuotaAction(
            key = testSlotKey,
            currentCount = 5,
            freeLimit = 3,
            isProActive = false
        )
        assertThat(allowedAtNewLimit).isFalse()
    }

    @Test
    fun `isFeatureAllowed returns true for Pro user`() = runTest {
        val allowed = rewardManager.isFeatureAllowed(
            key = testPassKey,
            isProActive = true,
            isProRequired = true
        )
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isFeatureAllowed returns true when Pro is not required`() = runTest {
        val allowed = rewardManager.isFeatureAllowed(
            key = testPassKey,
            isProActive = false,
            isProRequired = false
        )
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isFeatureAllowed returns false for free user without pass`() = runTest {
        val allowed = rewardManager.isFeatureAllowed(
            key = testPassKey,
            isProActive = false,
            isProRequired = true
        )
        assertThat(allowed).isFalse()
    }

    @Test
    fun `isFeatureAllowed returns true for free user with active pass`() = runTest {
        rewardManager.grantTimedPass(testPassKey)

        val allowed = rewardManager.isFeatureAllowed(
            key = testPassKey,
            isProActive = false,
            isProRequired = true
        )
        assertThat(allowed).isTrue()
    }

    @Test
    fun `ReminderQuotaManager delegation operates correctly`() = runTest {
        fakeAppConfigProvider.setLong(RewardManagerImpl.KEY_CONFIG_FREE_REMINDERS_LIMIT, 3L)

        // Free tier checks
        assertThat(
            rewardManager.canScheduleReminder(
                currentActiveCount = 2,
                isProActive = false
            )
        ).isTrue()
        assertThat(
            rewardManager.canScheduleReminder(
                currentActiveCount = 3,
                isProActive = false
            )
        ).isFalse()

        // Pro check
        assertThat(
            rewardManager.canScheduleReminder(
                currentActiveCount = 10,
                isProActive = true
            )
        ).isTrue()

        // Grant & consume pass at freeLimit (3)
        rewardManager.grantReminderPass()
        assertThat(rewardManager.getExtraSlotsCount(RewardManagerImpl.AiringReminderPassKey)).isEqualTo(
            1
        )
        assertThat(
            rewardManager.canScheduleReminder(
                currentActiveCount = 3,
                isProActive = false
            )
        ).isTrue()

        // Test ABOVE freeLimit (e.g. 4 reminders already active)
        assertThat(
            rewardManager.canScheduleReminder(
                currentActiveCount = 4,
                isProActive = false
            )
        ).isTrue()

        val consumed = rewardManager.consumeReminderPass()
        assertThat(consumed).isTrue()
        assertThat(
            rewardManager.canScheduleReminder(
                currentActiveCount = 3,
                isProActive = false
            )
        ).isFalse()
        assertThat(
            rewardManager.canScheduleReminder(
                currentActiveCount = 4,
                isProActive = false
            )
        ).isFalse()

        // Test 5th reminder workflow: 4 in DB, watch 1 ad -> grant 1 slot -> can schedule -> consume -> blocked
        rewardManager.grantReminderPass()
        assertThat(
            rewardManager.canScheduleReminder(
                currentActiveCount = 4,
                isProActive = false
            )
        ).isTrue()
        rewardManager.consumeReminderPass()
        assertThat(
            rewardManager.canScheduleReminder(
                currentActiveCount = 5,
                isProActive = false
            )
        ).isFalse()
    }
}
