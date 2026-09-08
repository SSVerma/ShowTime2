package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.shared.data.local.adapter.MediaTypeJsonAdapter
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class BacklogRepositoryTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockFirestore: FirebaseFirestore = mockk(relaxed = true)
    private val mockCollection: CollectionReference = mockk(relaxed = true)
    private val mockDocument: DocumentReference = mockk(relaxed = true)
    private val mockSnapshot: DocumentSnapshot = mockk(relaxed = true)
    private val mockKeyValueStorageClient: KeyValueStorageClient = mockk(relaxed = true)
    private val mockStorage: KeyValueStorage = mockk(relaxed = true)
    private val preferencesFlow =
        MutableStateFlow<androidx.datastore.preferences.core.Preferences>(emptyPreferences())
    private val gson = GsonBuilder()
        .registerTypeAdapter(MediaType::class.java, MediaTypeJsonAdapter())
        .create()

    private val testSeedJson = """
        [
          {
            "id": "curated_sight_and_sound",
            "title": "Sight & Sound Masterpieces",
            "description": "The defining cinematic milestones of world cinema history.",
            "category": "Curated",
            "mediaTypeFilter": "MOVIE",
            "targetCount": 10,
            "targetMediaItems": [
              {
                "id": 238,
                "title": "The Godfather",
                "mediaType": "movie",
                "posterImageUrl": "/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
                "releaseYear": "1972",
                "directorOrCreator": "Francis Ford Coppola",
                "voteAvg": 8.7
              }
            ],
            "isCustom": false
          },
          {
            "id": "curated_prestige_tv",
            "title": "Prestige TV Hall of Fame",
            "description": "The gold standard of serialized storytelling that redefined television.",
            "category": "Curated",
            "mediaTypeFilter": "TV",
            "targetCount": 8,
            "targetMediaItems": [
              {
                "id": 1396,
                "title": "Breaking Bad",
                "mediaType": "tv",
                "posterImageUrl": "/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg",
                "releaseYear": "2008",
                "directorOrCreator": "Vince Gilligan",
                "voteAvg": 8.9
              }
            ],
            "isCustom": false
          },
          {
            "id": "curated_nolan_villeneuve",
            "title": "Auteur Vision: Nolan & Villeneuve",
            "description": "Modern visionary epics blending grand spectacle with existential depth.",
            "category": "DirectorSpotlight",
            "mediaTypeFilter": "MOVIE",
            "targetCount": 6,
            "targetMediaItems": [
              {
                "id": 872585,
                "title": "Oppenheimer",
                "mediaType": "movie",
                "posterImageUrl": "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                "releaseYear": "2023",
                "directorOrCreator": "Christopher Nolan",
                "voteAvg": 8.1
              }
            ],
            "isCustom": false
          },
          {
            "id": "curated_century_sprint_2026",
            "title": "2026 Cinema Century Sprint",
            "description": "Watch 52 Movies or TV Shows this year (1 title per week).",
            "category": "PersonalGoal",
            "mediaTypeFilter": "ALL",
            "targetCount": 52,
            "targetMediaItems": [],
            "isCustom": false
          }
        ]
    """.trimIndent()

    private lateinit var repository: BacklogRepositoryImpl

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
        every { mockContext.assets.open("curated_challenges_seed.json") } answers {
            ByteArrayInputStream(testSeedJson.toByteArray())
        }

        every { mockSnapshot.exists() } returns false
        every { mockFirestore.collection(any()) } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocument
        every { mockDocument.set(any()) } returns Tasks.forResult(null)
        every { mockDocument.get() } returns Tasks.forResult(mockSnapshot)

        repository = BacklogRepositoryImpl(
            context = mockContext,
            firestore = mockFirestore,
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
    fun `getCuratedChallenges parses challenges from Firestore document when available`() =
        runTest {
            val remoteList = listOf(
                mapOf(
                    "id" to "curated_custom_remote",
                    "title" to "Sci-Fi Classics",
                    "description" to "Iconic sci-fi",
                    "category" to "Curated",
                    "mediaTypeFilter" to "MOVIE",
                    "targetCount" to 5,
                    "targetMediaItems" to emptyList<Any>(),
                    "isCustom" to false
                )
            )
            every { mockSnapshot.exists() } returns true
            every { mockSnapshot.get("challenges") } returns remoteList

            val curated = repository.getCuratedChallenges(forceRefresh = true)

            assertThat(curated).isNotEmpty()
            assertThat(curated.first().id).isEqualTo("curated_custom_remote")
            assertThat(curated.first().title).isEqualTo("Sci-Fi Classics")
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

    @Test
    fun `gson serializes and deserializes CinephileChallenge with MediaType`() = runTest {
        val challenges = repository.getCuratedChallenges()
        val json = gson.toJson(challenges)
        val type = object : TypeToken<List<CinephileChallenge>>() {}.type
        val parsed: List<CinephileChallenge> = gson.fromJson(json, type)
        assertThat(parsed).isNotEmpty()
        assertThat(parsed.first().targetMediaItems.first().mediaType).isEqualTo(MediaType.Movie)
    }

    @Test
    fun `updateCustomChallenge updates existing challenge in storage`() = runTest {
        val custom = repository.createCustomChallenge(
            title = "Original Title",
            description = "Original Description",
            mediaTypeFilter = ChallengeMediaTypeFilter.MOVIE,
            targetCount = 5,
            targetItems = emptyList()
        )

        val updated = custom.copy(title = "Updated Title", description = "Updated Description")
        repository.updateCustomChallenge(updated)
        assertThat(updated.title).isEqualTo("Updated Title")
    }

    @Test
    fun `restoreBacklog merges remote challenges without overwriting unbacked local goals`() =
        runTest {
            val localGoal = repository.createCustomChallenge(
                title = "Local Only Goal",
                description = "Created locally",
                mediaTypeFilter = ChallengeMediaTypeFilter.MOVIE,
                targetCount = 5,
                targetItems = emptyList()
            )

            val remoteChallenge = CinephileChallenge(
                id = "remote_goal_1",
                title = "Remote Goal",
                description = "From cloud backup",
                category = ChallengeCategory.Curated,
                mediaTypeFilter = ChallengeMediaTypeFilter.MOVIE,
                targetCount = 10,
                targetMediaItems = emptyList(),
                isCustom = false
            )

            repository.restoreBacklog(
                activeChallenges = listOf(remoteChallenge),
                blindspots = emptyList()
            )

            val active = repository.activeChallengesFlow.first()
            assertThat(active.any { it.id == localGoal.id }).isTrue()
            assertThat(active.any { it.id == "remote_goal_1" }).isTrue()
        }

    @Test
    fun `restoreBacklog resolves conflicting challenge IDs by keeping richer goal`() = runTest {
        val item1 = ChallengeMediaItem(
            id = 101,
            title = "Movie 1",
            mediaType = MediaType.Movie,
            posterImageUrl = "",
            releaseYear = "2020",
            voteAvg = 8.0f
        )
        val item2 = ChallengeMediaItem(
            id = 102,
            title = "Movie 2",
            mediaType = MediaType.Movie,
            posterImageUrl = "",
            releaseYear = "2021",
            voteAvg = 8.5f
        )

        // Local goal has 1 item
        val localGoal = repository.createCustomChallenge(
            title = "Custom Marathon",
            description = "Local version",
            mediaTypeFilter = ChallengeMediaTypeFilter.MOVIE,
            targetCount = 2,
            targetItems = listOf(item1)
        )

        // Remote goal with same ID has 2 items (richer)
        val richerRemote = localGoal.copy(
            description = "Remote richer version",
            targetMediaItems = listOf(item1, item2)
        )

        repository.restoreBacklog(
            activeChallenges = listOf(richerRemote),
            blindspots = emptyList()
        )

        val active = repository.activeChallengesFlow.first()
        val merged = active.first { it.id == localGoal.id }
        assertThat(merged.targetMediaItems).hasSize(2)
        assertThat(merged.description).isEqualTo("Remote richer version")
    }

    @Test
    fun `restoreBacklog merges remote blindspots without duplicates`() = runTest {
        val blindspot1 = BlindspotPriorityItem(
            mediaId = 500,
            mediaType = MediaType.Movie,
            title = "Citizen Kane",
            posterImageUrl = "/poster1.jpg",
            releaseYear = "1941",
            voteAvg = 8.3f,
            addedAt = 1000L
        )
        val blindspot2 = BlindspotPriorityItem(
            mediaId = 600,
            mediaType = MediaType.Tv,
            title = "The Wire",
            posterImageUrl = "/poster2.jpg",
            releaseYear = "2002",
            voteAvg = 9.3f,
            addedAt = 2000L
        )

        repository.addBlindspot(blindspot1)

        // Restore list contains duplicate blindspot1 and new blindspot2
        repository.restoreBacklog(
            activeChallenges = emptyList(),
            blindspots = listOf(blindspot1, blindspot2)
        )

        val blindspots = repository.blindspotsFlow.first()
        assertThat(blindspots).hasSize(2)
        assertThat(blindspots.map { it.mediaId }).containsExactly(500, 600)
    }
}
