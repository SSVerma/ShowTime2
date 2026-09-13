package com.ssverma.shared.data.repository

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.Preferences
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.shared.domain.model.feature.CinephileFeature
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DefaultAppConfigRepositoryTest {

    private val mockStorage: KeyValueStorage = mockk(relaxed = true)
    private val mockAppConfigProvider: AppConfigProvider = mockk(relaxed = true)
    private val preferencesFlow = MutableStateFlow<Preferences>(emptyPreferences())

    private lateinit var repository: DefaultAppConfigRepository

    @Before
    fun setUp() {
        preferencesFlow.value = emptyPreferences()
        every { mockStorage.data } returns preferencesFlow
        coEvery { mockStorage.updateData(any()) } coAnswers {
            val transform = firstArg<suspend (Preferences) -> Preferences>()
            val updated = transform(preferencesFlow.value)
            preferencesFlow.value = updated
            updated
        }

        repository = DefaultAppConfigRepository(
            keyValueStorage = mockStorage,
            appConfigProvider = mockAppConfigProvider
        )
    }

    @Test
    fun `acknowledgedFeatures returns empty set initially`() = runTest {
        val features = repository.acknowledgedFeatures.first()
        assertThat(features).isEmpty()
    }

    @Test
    fun `acknowledgeFeature persists and emits updated feature set`() = runTest {
        repository.acknowledgeFeature(CinephileFeature.CINEMA_DIARY.id)

        val updated = repository.acknowledgedFeatures.first()
        assertThat(updated).contains(CinephileFeature.CINEMA_DIARY.id)
        assertThat(CinephileFeature.CINEMA_DIARY.isNew(updated)).isFalse()
        assertThat(CinephileFeature.MOVIE_MATCH.isNew(updated)).isTrue()
    }

    @Test
    fun `acknowledgeFeature accumulates multiple features`() = runTest {
        repository.acknowledgeFeature(CinephileFeature.CINEMA_DIARY.id)
        repository.acknowledgeFeature(CinephileFeature.MOVIE_MATCH.id)

        val updated = repository.acknowledgedFeatures.first()
        assertThat(updated).containsExactly(
            CinephileFeature.CINEMA_DIARY.id,
            CinephileFeature.MOVIE_MATCH.id
        )
    }

    @Test
    fun `lastSeenVersionCode returns 0 initially`() = runTest {
        val versionCode = repository.lastSeenVersionCode.first()
        assertThat(versionCode).isEqualTo(0)
    }

    @Test
    fun `updateLastSeenVersionCode persists and emits updated versionCode`() = runTest {
        repository.updateLastSeenVersionCode(20000)

        val updated = repository.lastSeenVersionCode.first()
        assertThat(updated).isEqualTo(20000)
    }

    @Test
    fun `lastSeenWhatsNewCampaign returns empty string initially`() = runTest {
        val campaign = repository.lastSeenWhatsNewCampaign.first()
        assertThat(campaign).isEmpty()
    }

    @Test
    fun `updateLastSeenWhatsNewCampaign persists and emits updated campaign`() = runTest {
        repository.updateLastSeenWhatsNewCampaign("2.0.0")

        val updated = repository.lastSeenWhatsNewCampaign.first()
        assertThat(updated).isEqualTo("2.0.0")
    }

    @Test
    fun `isWhatsNewEnabled observes remote config`() = runTest {
        every { mockAppConfigProvider.observeBoolean("whats_new_enabled", true) } returns
                MutableStateFlow(true)

        val enabled = repository.isWhatsNewEnabled.first()
        assertThat(enabled).isTrue()
    }
}
