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
}
