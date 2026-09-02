package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BacklogRepositoryTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockKeyValueStorageClient: KeyValueStorageClient = mockk(relaxed = true)
    private val mockStorage: KeyValueStorage = mockk(relaxed = true)
    private val gson = Gson()

    private lateinit var repository: BacklogRepositoryImpl

    @Before
    fun setUp() {
        every { mockKeyValueStorageClient.createKeyValueStorage(any(), any()) } returns mockStorage
        every { mockStorage.data } returns flowOf(emptyPreferences())

        repository = BacklogRepositoryImpl(
            context = mockContext,
            keyValueStorageClient = mockKeyValueStorageClient
        )
    }

    @Test
    fun `getCuratedChallenges returns rich curated catalog with Movie and TV parity`() = runTest {
        val curated = repository.getCuratedChallenges()

        assertThat(curated).isNotEmpty()
        assertThat(curated.any { it.mediaTypeFilter == ChallengeMediaTypeFilter.MOVIE }).isTrue()
        assertThat(curated.any { it.mediaTypeFilter == ChallengeMediaTypeFilter.TV }).isTrue()
        assertThat(curated.any { it.category == ChallengeCategory.Curated }).isTrue()
        assertThat(curated.any { it.category == ChallengeCategory.DirectorSpotlight }).isTrue()
    }

    @Test
    fun `createCustomChallenge creates valid challenge with custom ID`() = runTest {
        val custom = repository.createCustomChallenge(
            title = "Noir Marathon",
            description = "10 gritty crime noir movies",
            mediaTypeFilter = ChallengeMediaTypeFilter.MOVIE,
            targetCount = 10,
            targetItems = emptyList()
        )

        assertThat(custom.title).isEqualTo("Noir Marathon")
        assertThat(custom.isCustom).isTrue()
        assertThat(custom.targetCount).isEqualTo(10)
        assertThat(custom.id).startsWith("custom_")
    }
}
