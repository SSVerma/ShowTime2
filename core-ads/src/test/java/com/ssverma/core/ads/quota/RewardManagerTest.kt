package com.ssverma.core.ads.quota

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.testing.fakes.FakeAppConfigProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RewardManagerTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val fakeAppConfigProvider = FakeAppConfigProvider()
    private val mockKeyValueStorageClient: KeyValueStorageClient = mockk(relaxed = true)
    private val mockStorage: KeyValueStorage = mockk(relaxed = true)
    private val preferencesFlow = MutableStateFlow(emptyPreferences())

    private lateinit var rewardManager: RewardManagerImpl

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
    fun `canCreateCustomList returns true for pro user regardless of limit`() = runTest {
        fakeAppConfigProvider.setLong(RewardManagerImpl.KEY_CONFIG_FREE_CUSTOM_LIST_LIMIT, 3L)

        val allowed = rewardManager.canCreateCustomList(currentCount = 10, isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `canCreateCustomList respects free limit for free user`() = runTest {
        fakeAppConfigProvider.setLong(RewardManagerImpl.KEY_CONFIG_FREE_CUSTOM_LIST_LIMIT, 3L)

        val allowedUnderLimit =
            rewardManager.canCreateCustomList(currentCount = 2, isProActive = false)
        assertThat(allowedUnderLimit).isTrue()

        val allowedAtLimit =
            rewardManager.canCreateCustomList(currentCount = 3, isProActive = false)
        assertThat(allowedAtLimit).isFalse()
    }

    @Test
    fun `canPublishCommunityList returns true for pro user`() = runTest {
        fakeAppConfigProvider.setLong(RewardManagerImpl.KEY_CONFIG_FREE_PUBLISH_LIMIT, 2L)

        val allowed =
            rewardManager.canPublishCommunityList(currentActiveCount = 5, isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isAutoBackupAllowed returns true for pro user`() = runTest {
        val allowed = rewardManager.isAutoBackupAllowed(isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isTraktSyncAllowed returns true for pro user`() = runTest {
        val allowed = rewardManager.isTraktSyncAllowed(isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isTraktSyncAllowed returns false for free user when no pass active and pro required`() =
        runTest {
            fakeAppConfigProvider.setBoolean(
                RewardManagerImpl.KEY_CONFIG_TRAKT_SYNC_PRO_REQUIRED,
                true
            )
            val allowed = rewardManager.isTraktSyncAllowed(isProActive = false)
            assertThat(allowed).isFalse()
        }

    @Test
    fun `isMultiServiceFilterAllowed returns true for pro user`() = runTest {
        val allowed = rewardManager.isMultiServiceFilterAllowed(isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isMultiServiceFilterAllowed returns false for free user with no active pass`() = runTest {
        val allowed = rewardManager.isMultiServiceFilterAllowed(isProActive = false)
        assertThat(allowed).isFalse()
    }

    @Test
    fun `isTasteAnalyticsAllowed returns true for pro user`() = runTest {
        val allowed = rewardManager.isTasteAnalyticsAllowed(isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isTasteAnalyticsAllowed returns false for free user with no active pass`() = runTest {
        val allowed = rewardManager.isTasteAnalyticsAllowed(isProActive = false)
        assertThat(allowed).isFalse()
    }

    @Test
    fun `isReceiptWatermarkFreeAllowed returns true for pro user`() = runTest {
        val allowed = rewardManager.isReceiptWatermarkFreeAllowed(isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isReceiptWatermarkFreeAllowed returns false for free user with no active pass`() =
        runTest {
            val allowed = rewardManager.isReceiptWatermarkFreeAllowed(isProActive = false)
            assertThat(allowed).isFalse()
        }

    @Test
    fun `isWrappedStoryAllowed returns true for pro user`() = runTest {
        val allowed = rewardManager.isWrappedStoryAllowed(isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isWrappedStoryAllowed returns false for free user with no active pass`() = runTest {
        val allowed = rewardManager.isWrappedStoryAllowed(isProActive = false)
        assertThat(allowed).isFalse()
    }

    @Test
    fun `grantRewardPass for CINEMA_WRAPPED_STORY unlocks wrapped story pass`() = runTest {
        rewardManager.grantRewardPass(RewardPassType.CINEMA_WRAPPED_STORY)
        val status = rewardManager.passStatus.value
        assertThat(status.isWrappedStoryUnlocked).isTrue()
        assertThat(status.wrappedStoryExpiryTimestamp).isGreaterThan(System.currentTimeMillis())

        val allowed = rewardManager.isWrappedStoryAllowed(isProActive = false)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `canScheduleReminder returns true for pro user regardless of count`() = runTest {
        fakeAppConfigProvider.setLong(RewardManagerImpl.KEY_CONFIG_FREE_REMINDERS_LIMIT, 3L)
        val allowed = rewardManager.canScheduleReminder(currentActiveCount = 10, isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `canScheduleReminder respects free limit for free user`() = runTest {
        fakeAppConfigProvider.setLong(RewardManagerImpl.KEY_CONFIG_FREE_REMINDERS_LIMIT, 3L)
        val underLimit =
            rewardManager.canScheduleReminder(currentActiveCount = 2, isProActive = false)
        assertThat(underLimit).isTrue()

        val atLimit = rewardManager.canScheduleReminder(currentActiveCount = 3, isProActive = false)
        assertThat(atLimit).isFalse()
    }

    @Test
    fun `grantRewardPass for AIRING_REMINDERS unlocks airing reminders pass`() = runTest {
        rewardManager.grantRewardPass(RewardPassType.AIRING_REMINDERS)
        val status = rewardManager.passStatus.value
        assertThat(status.isAiringRemindersUnlocked).isTrue()
        assertThat(status.airingRemindersExpiryTimestamp).isGreaterThan(System.currentTimeMillis())

        val allowed =
            rewardManager.canScheduleReminder(currentActiveCount = 10, isProActive = false)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isMatchRoomAllowed returns true for pro user`() = runTest {
        val allowed = rewardManager.isMatchRoomAllowed(isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isMatchRoomAllowed returns false for free user without pass`() = runTest {
        val allowed = rewardManager.isMatchRoomAllowed(isProActive = false)
        assertThat(allowed).isFalse()
    }

    @Test
    fun `grantRewardPass for MATCH_ROOM unlocks match room pass`() = runTest {
        rewardManager.grantRewardPass(RewardPassType.MATCH_ROOM)
        val status = rewardManager.passStatus.value
        assertThat(status.isMatchRoomUnlocked).isTrue()
        assertThat(status.matchRoomExpiryTimestamp).isGreaterThan(System.currentTimeMillis())

        val allowed = rewardManager.isMatchRoomAllowed(isProActive = false)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isListShareThemesAllowed returns true for pro user`() = runTest {
        val allowed = rewardManager.isListShareThemesAllowed(isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `isListShareThemesAllowed returns false for free user without pass`() = runTest {
        val allowed = rewardManager.isListShareThemesAllowed(isProActive = false)
        assertThat(allowed).isFalse()
    }

    @Test
    fun `grantRewardPass for LIST_SHARE_THEMES unlocks list share themes pass`() = runTest {
        rewardManager.grantRewardPass(RewardPassType.LIST_SHARE_THEMES)
        val status = rewardManager.passStatus.value
        assertThat(status.isListShareThemesUnlocked).isTrue()
        assertThat(status.listShareThemesExpiryTimestamp).isGreaterThan(System.currentTimeMillis())

        val allowed = rewardManager.isListShareThemesAllowed(isProActive = false)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `grantRewardPass for MULTI_SERVICE_FILTER respects minutes configuration`() = runTest {
        fakeAppConfigProvider.setLong(
            RewardManagerImpl.KEY_CONFIG_REWARDED_MULTI_SERVICE_MINUTES,
            120L
        )
        val before = System.currentTimeMillis()
        rewardManager.grantRewardPass(RewardPassType.MULTI_SERVICE_FILTER)
        val status = rewardManager.passStatus.value
        assertThat(status.isMultiServiceUnlocked).isTrue()
        val deltaMinutes =
            TimeUnit.MILLISECONDS.toMinutes(status.multiServiceExpiryTimestamp - before)
        assertThat(deltaMinutes).isEqualTo(120L)

        val allowed = rewardManager.isMultiServiceFilterAllowed(isProActive = false)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `canCreateCustomGoal returns true for pro user regardless of limit`() = runTest {
        fakeAppConfigProvider.setLong(RewardManagerImpl.KEY_CONFIG_FREE_CUSTOM_GOAL_LIMIT, 2L)

        val allowed = rewardManager.canCreateCustomGoal(currentActiveCount = 10, isProActive = true)
        assertThat(allowed).isTrue()
    }

    @Test
    fun `canCreateCustomGoal respects free limit for free user`() = runTest {
        fakeAppConfigProvider.setLong(RewardManagerImpl.KEY_CONFIG_FREE_CUSTOM_GOAL_LIMIT, 2L)

        val allowedUnderLimit =
            rewardManager.canCreateCustomGoal(currentActiveCount = 1, isProActive = false)
        assertThat(allowedUnderLimit).isTrue()

        val allowedAtLimit =
            rewardManager.canCreateCustomGoal(currentActiveCount = 2, isProActive = false)
        assertThat(allowedAtLimit).isFalse()
    }

    @Test
    fun `grantRewardPass for EXTRA_CUSTOM_GOAL increments custom goal slots`() = runTest {
        fakeAppConfigProvider.setLong(RewardManagerImpl.KEY_CONFIG_FREE_CUSTOM_GOAL_LIMIT, 2L)

        rewardManager.grantRewardPass(RewardPassType.EXTRA_CUSTOM_GOAL)
        val status = rewardManager.passStatus.value
        assertThat(status.extraCustomGoalSlots).isEqualTo(1)

        val allowedWithBonus =
            rewardManager.canCreateCustomGoal(currentActiveCount = 2, isProActive = false)
        assertThat(allowedWithBonus).isTrue()

        val blockedAboveBonus =
            rewardManager.canCreateCustomGoal(currentActiveCount = 3, isProActive = false)
        assertThat(blockedAboveBonus).isFalse()
    }
}
