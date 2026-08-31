package com.ssverma.core.ads.quota

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.testing.fakes.FakeAppConfigProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RewardManagerTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val fakeAppConfigProvider = FakeAppConfigProvider()
    private val mockKeyValueStorageClient: KeyValueStorageClient = mockk(relaxed = true)
    private val mockStorage: KeyValueStorage = mockk(relaxed = true)

    private lateinit var rewardManager: RewardManagerImpl

    @Before
    fun setUp() {
        every { mockKeyValueStorageClient.createKeyValueStorage(any(), any()) } returns mockStorage
        every { mockStorage.data } returns flowOf(emptyPreferences())

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
}
