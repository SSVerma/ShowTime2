package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.testing.fakes.FakeAppConfigProvider
import com.ssverma.shared.domain.model.AppUpdateStatus
import com.ssverma.shared.domain.utils.AppConfigConstants
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AppUpdateRepositoryTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockKeyValueStorageClient: KeyValueStorageClient = mockk(relaxed = true)
    private val mockStorage: KeyValueStorage = mockk(relaxed = true)
    private val preferencesFlow = MutableStateFlow(emptyPreferences())

    private lateinit var fakeAppConfigProvider: FakeAppConfigProvider
    private lateinit var repository: AppUpdateRepositoryImpl

    @Before
    fun setUp() {
        fakeAppConfigProvider = FakeAppConfigProvider()
        every { mockKeyValueStorageClient.createKeyValueStorage(any(), any()) } returns mockStorage
        every { mockStorage.data } returns preferencesFlow

        repository = AppUpdateRepositoryImpl(
            context = mockContext,
            appConfigProvider = fakeAppConfigProvider,
            keyValueStorageClient = mockKeyValueStorageClient
        )
    }

    @Test
    fun `observeAppUpdateStatus emits UpToDate when remote thresholds are 0`() = runTest {
        val status = repository.observeAppUpdateStatus(currentVersionCode = 20L).first()
        assertThat(status).isEqualTo(AppUpdateStatus.UpToDate)
    }

    @Test
    fun `observeAppUpdateStatus emits ForceUpdate when min_supported_version_code exceeds current`() =
        runTest {
            fakeAppConfigProvider.setLong(
                key = AppConfigConstants.KEY_MIN_SUPPORTED_VERSION_CODE,
                value = 25L
            )
            fakeAppConfigProvider.setString(
                key = AppConfigConstants.KEY_UPDATE_TITLE,
                value = "Critical Security Update"
            )
            fakeAppConfigProvider.setString(
                key = AppConfigConstants.KEY_UPDATE_MESSAGE,
                value = "Please update to version 25"
            )

            val status = repository.observeAppUpdateStatus(currentVersionCode = 20L).first()

            assertThat(status).isInstanceOf(AppUpdateStatus.ForceUpdate::class.java)
            val forceUpdate = status as AppUpdateStatus.ForceUpdate
            assertThat(forceUpdate.currentVersionCode).isEqualTo(20L)
            assertThat(forceUpdate.minSupportedVersionCode).isEqualTo(25L)
            assertThat(forceUpdate.title).isEqualTo("Critical Security Update")
            assertThat(forceUpdate.message).isEqualTo("Please update to version 25")
        }

    @Test
    fun `observeAppUpdateStatus emits SoftUpdate when latest_version_code exceeds current and not dismissed`() =
        runTest {
            fakeAppConfigProvider.setLong(
                key = AppConfigConstants.KEY_LATEST_VERSION_CODE,
                value = 25L
            )
            fakeAppConfigProvider.setString(
                key = AppConfigConstants.KEY_UPDATE_TITLE,
                value = "New Version 2.5"
            )

            val status = repository.observeAppUpdateStatus(currentVersionCode = 20L).first()

            assertThat(status).isInstanceOf(AppUpdateStatus.SoftUpdate::class.java)
            val softUpdate = status as AppUpdateStatus.SoftUpdate
            assertThat(softUpdate.currentVersionCode).isEqualTo(20L)
            assertThat(softUpdate.latestVersionCode).isEqualTo(25L)
            assertThat(softUpdate.title).isEqualTo("New Version 2.5")
        }

    @Test
    fun `observeAppUpdateStatus emits UpToDate when latest_version_code was dismissed within cooldown`() =
        runTest {
            val versionKey = longPreferencesKey("last_dismissed_soft_update_version")
            val timestampKey = longPreferencesKey("last_dismissed_soft_update_timestamp")
            // Dismissed 2 days ago
            val twoDaysAgo = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
            preferencesFlow.value = mutablePreferencesOf(
                versionKey to 25L,
                timestampKey to twoDaysAgo
            )

            fakeAppConfigProvider.setLong(
                key = AppConfigConstants.KEY_LATEST_VERSION_CODE,
                value = 25L
            )

            val status = repository.observeAppUpdateStatus(currentVersionCode = 20L).first()

            assertThat(status).isEqualTo(AppUpdateStatus.UpToDate)
        }

    @Test
    fun `observeAppUpdateStatus emits SoftUpdate when latest_version_code cooldown of 7 days has expired`() =
        runTest {
            val versionKey = longPreferencesKey("last_dismissed_soft_update_version")
            val timestampKey = longPreferencesKey("last_dismissed_soft_update_timestamp")
            // Dismissed 8 days ago
            val eightDaysAgo = System.currentTimeMillis() - (8 * 24 * 60 * 60 * 1000L)
            preferencesFlow.value = mutablePreferencesOf(
                versionKey to 25L,
                timestampKey to eightDaysAgo
            )

            fakeAppConfigProvider.setLong(
                key = AppConfigConstants.KEY_LATEST_VERSION_CODE,
                value = 25L
            )

            val status = repository.observeAppUpdateStatus(currentVersionCode = 20L).first()

            assertThat(status).isInstanceOf(AppUpdateStatus.SoftUpdate::class.java)
        }

    @Test
    fun `checkAppUpdateStatus returns ForceUpdate when min version exceeds current`() = runTest {
        fakeAppConfigProvider.setLong(
            key = AppConfigConstants.KEY_MIN_SUPPORTED_VERSION_CODE,
            value = 30L
        )

        val status = repository.checkAppUpdateStatus(currentVersionCode = 20L)

        assertThat(status).isInstanceOf(AppUpdateStatus.ForceUpdate::class.java)
        val forceUpdate = status as AppUpdateStatus.ForceUpdate
        assertThat(forceUpdate.minSupportedVersionCode).isEqualTo(30L)
    }
}
