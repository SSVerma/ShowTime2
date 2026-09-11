package com.ssverma.feature.library.ui.share

import android.content.Context
import android.content.SharedPreferences
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.library.SecretSharedList
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.repository.SecretSharedListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SecretSharedListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val mockSecretSharedListRepository: SecretSharedListRepository = mockk(relaxed = true)
    private val mockLibraryRepository: LibraryRepository = mockk(relaxed = true)
    private val mockTmdbApiService: TmdbApiService = mockk(relaxed = true)
    private val mockGoogleAuthClient: GoogleAuthClient = mockk(relaxed = true)
    private val mockContext: Context = mockk(relaxed = true)
    private val mockPrefs: SharedPreferences = mockk(relaxed = true)

    private lateinit var viewModel: SecretSharedListViewModel

    @Before
    fun setUp() {
        every {
            mockContext.getSharedPreferences(
                "showtime_device_prefs",
                Context.MODE_PRIVATE
            )
        } returns mockPrefs
        every { mockPrefs.getString("persistent_user_uuid", "") } returns "device-user-123"
        every { mockPrefs.getString("persistent_user_uuid", any()) } returns "device-user-123"
        every { mockPrefs.getString("user_display_name", null) } returns "Friend"
        every { mockGoogleAuthClient.currentUser } returns MutableStateFlow(null)
        every { mockGoogleAuthClient.currentFirebaseAuthUid } returns null

        every { mockContext.getString(R.string.secret_share_all_added_success) } returns "All items added to your Watchlist!"
        every { mockContext.getString(R.string.secret_share_cloned_success) } returns "List cloned to your Custom Lists!"
        every { mockContext.getString(R.string.secret_share_item_added_success, any()) } answers {
            val title = (args[1] as? Array<*>)?.firstOrNull()?.toString() ?: args[1].toString()
            "\"$title\" added to list!"
        }
        every { mockContext.getString(R.string.secret_share_collab_enabled_msg) } returns "Friends can now add titles to this list"
        every { mockContext.getString(R.string.secret_share_collab_disabled_msg) } returns "List is now view-only for friends"
        every { mockContext.getString(R.string.secret_share_sync_failed) } returns "Failed to sync changes. Reverted."

        coEvery {
            mockSecretSharedListRepository.addMediaToSharedList(any(), any())
        } returns Result.Success(Unit)
        coEvery {
            mockSecretSharedListRepository.removeMediaFromSharedList(any(), any())
        } returns Result.Success(Unit)

        viewModel = SecretSharedListViewModel(
            secretSharedListRepository = mockSecretSharedListRepository,
            libraryRepository = mockLibraryRepository,
            tmdbApiService = mockTmdbApiService,
            googleAuthClient = mockGoogleAuthClient,
            context = mockContext
        )
    }

    @Test
    fun `init loads and observes secret shared list`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SL-SECRET999",
            title = "Nolan Filmography",
            description = "All Christopher Nolan masterworks",
            ownerUserId = "device-user-123",
            ownerName = "Curator",
            isCollaborative = true,
            createdAtEpochMs = 123456789L,
            items = listOf(
                SecretSharedListItem(
                    mediaId = 157336,
                    mediaType = MediaType.Movie,
                    title = "Interstellar",
                    posterImageUrl = "/path.jpg",
                    voteAvg = 8.6f
                )
            )
        )

        every { mockSecretSharedListRepository.observeSecretSharedList("SL-SECRET999") } returns flowOf(
            sampleList
        )

        viewModel.init("SL-SECRET999")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleList, state.secretSharedList)
        assertTrue(state.isOwner)
        assertTrue(state.isCollaborative)
        assertFalse(state.isRevoked)
        assertEquals("device-user-123", state.currentUserId)
    }

    @Test
    fun `addAllToWatchlist invokes library repository for items not yet in watchlist`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SL-CODE1",
            title = "Watchlist Seed",
            ownerUserId = "other-user",
            ownerName = "Friend",
            items = listOf(
                SecretSharedListItem(
                    mediaId = 101,
                    mediaType = MediaType.Movie,
                    title = "Inception",
                    posterImageUrl = "/incept.jpg",
                    backdropImageUrl = "/back.jpg",
                    voteAvg = 8.8f,
                    releaseYear = "2010"
                ),
                SecretSharedListItem(
                    mediaId = 102,
                    mediaType = MediaType.Movie,
                    title = "Memento",
                    posterImageUrl = "/mem.jpg",
                    backdropImageUrl = "/back2.jpg",
                    voteAvg = 8.4f,
                    releaseYear = "2000"
                )
            )
        )

        every { mockSecretSharedListRepository.observeSecretSharedList("SL-CODE1") } returns flowOf(
            sampleList
        )
        viewModel.init("SL-CODE1")
        advanceUntilIdle()

        every { mockLibraryRepository.isInWatchlistFlow(101) } returns flowOf(false)
        every { mockLibraryRepository.isInWatchlistFlow(102) } returns flowOf(true)

        viewModel.addAllToWatchlist()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            mockLibraryRepository.toggleWatchlist(
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Inception",
                posterImageUrl = "/incept.jpg",
                backdropImageUrl = "/back.jpg",
                voteAvg = 8.8f,
                releaseDate = "2010"
            )
        }
        coVerify(exactly = 0) {
            mockLibraryRepository.toggleWatchlist(
                mediaId = 102,
                mediaType = any(),
                title = any(),
                posterImageUrl = any(),
                backdropImageUrl = any(),
                voteAvg = any(),
                releaseDate = any()
            )
        }
        assertEquals("All items added to your Watchlist!", viewModel.uiState.value.feedbackMessage)
    }

    @Test
    fun `cloneToMyLists creates custom list and adds all media`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SL-CLONE1",
            title = "Cyberpunk Gems",
            description = "Neon sci-fi",
            ownerUserId = "other-user",
            ownerName = "Friend",
            items = listOf(
                SecretSharedListItem(
                    mediaId = 550,
                    mediaType = MediaType.Movie,
                    title = "Blade Runner 2049",
                    posterImageUrl = "/br.jpg",
                    backdropImageUrl = "/br_back.jpg",
                    voteAvg = 8.0f
                )
            )
        )

        every { mockSecretSharedListRepository.observeSecretSharedList("SL-CLONE1") } returns flowOf(
            sampleList
        )
        coEvery {
            mockLibraryRepository.createCustomList(
                title = any(),
                description = any(),
                coverImageUrl = any(),
                isCloned = any(),
                sourceAuthorName = any()
            )
        } returns "custom-list-42"

        viewModel.init("SL-CLONE1")
        advanceUntilIdle()

        viewModel.cloneToMyLists()
        advanceUntilIdle()

        coVerify {
            mockLibraryRepository.createCustomList(
                title = "Cyberpunk Gems",
                description = "Neon sci-fi",
                coverImageUrl = "/br.jpg",
                isCloned = true,
                sourceAuthorName = "Friend"
            )
            mockLibraryRepository.addMediaToCustomList(
                listId = "custom-list-42",
                mediaId = 550,
                mediaType = MediaType.Movie,
                title = "Blade Runner 2049",
                posterImageUrl = "/br.jpg",
                backdropImageUrl = "/br_back.jpg",
                voteAvg = 8.0f
            )
        }
        assertEquals("List cloned to your Custom Lists!", viewModel.uiState.value.feedbackMessage)
    }

    @Test
    fun `searchMedia queries TMDB and filters movies and tv shows`() = runTest {
        val suggestionMovie = RemoteMultiSearchSuggestion(
            mediaType = "movie",
            id = 11,
            name = "Star Wars",
            popularity = 50f,
            profilePath = null,
            department = null,
            gender = 0,
            backdropPath = null,
            posterPath = "/starwars.jpg",
            overview = "A galaxy far far away",
            videoAvailable = false,
            voteAvg = 8.2f,
            voteCount = 1000,
            originalLanguage = "en",
            releaseDate = "1977-05-25",
            firstAirDate = null
        )
        val suggestionPerson = RemoteMultiSearchSuggestion(
            mediaType = "person",
            id = 12,
            name = "George Lucas",
            popularity = 20f,
            profilePath = null,
            department = "Directing",
            gender = 2,
            backdropPath = null,
            posterPath = null,
            overview = null,
            videoAvailable = false,
            voteAvg = 0f,
            voteCount = 0,
            originalLanguage = null,
            releaseDate = null,
            firstAirDate = null
        )
        val pagedPayload = PagedPayload(
            id = 0,
            page = 1,
            pageCount = 1,
            resultCount = 2,
            results = listOf(suggestionMovie, suggestionPerson)
        )

        coEvery { mockTmdbApiService.multiSearch("Star Wars") } returns ApiResponse.Success(
            body = pagedPayload,
            payload = mockk(relaxed = true)
        )

        viewModel.searchMedia("Star Wars")
        advanceUntilIdle()

        val results = viewModel.uiState.value.searchResults
        assertEquals(1, results.size)
        assertEquals(11, results.first().mediaId)
        assertEquals("Star Wars", results.first().title)
        assertEquals("1977", results.first().releaseYear)
        assertEquals(MediaType.Movie, results.first().mediaType)
        assertEquals("Friend", results.first().addedByName)
        assertEquals("device-user-123", results.first().addedByUserId)
    }

    @Test
    fun `addMediaToSharedList and removeMediaFromSharedList call repository`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SL-SHARE1",
            title = "Test",
            ownerUserId = "u1",
            ownerName = "O"
        )
        every { mockSecretSharedListRepository.observeSecretSharedList("SL-SHARE1") } returns flowOf(
            sampleList
        )

        viewModel.init("SL-SHARE1")
        advanceUntilIdle()

        val itemToAdd = SecretSharedListItem(
            mediaId = 99,
            mediaType = MediaType.Movie,
            title = "Dune",
            posterImageUrl = "/dune.jpg",
            voteAvg = 8.1f
        )
        viewModel.addMediaToSharedList(itemToAdd)
        advanceUntilIdle()

        coVerify {
            mockSecretSharedListRepository.addMediaToSharedList(
                "SL-SHARE1",
                match { it.mediaId == 99 && it.title == "Dune" && it.addedByName == "Friend" }
            )
        }
        assertEquals("\"Dune\" added to list!", viewModel.uiState.value.feedbackMessage)

        viewModel.removeMediaFromSharedList(99)
        advanceUntilIdle()

        coVerify { mockSecretSharedListRepository.removeMediaFromSharedList("SL-SHARE1", 99) }
    }

    @Test
    fun `addMediaToSharedList optimistically updates UI state and commits on success`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SL-OPT1",
            title = "Optimistic Test",
            ownerUserId = "u1",
            ownerName = "O",
            items = emptyList()
        )
        every { mockSecretSharedListRepository.observeSecretSharedList("SL-OPT1") } returns flowOf(
            sampleList
        )
        coEvery {
            mockSecretSharedListRepository.addMediaToSharedList(
                "SL-OPT1",
                any()
            )
        } returns Result.Success(Unit)

        viewModel.init("SL-OPT1")
        advanceUntilIdle()

        val itemToAdd = SecretSharedListItem(
            mediaId = 55,
            mediaType = MediaType.Movie,
            title = "Oppenheimer",
            posterImageUrl = "/oppenheimer.jpg"
        )

        viewModel.addMediaToSharedList(itemToAdd)

        // Verify optimistic update immediately applied
        assertEquals(1, viewModel.uiState.value.secretSharedList?.items?.size)
        assertEquals("Oppenheimer", viewModel.uiState.value.secretSharedList?.items?.first()?.title)
        assertTrue(viewModel.uiState.value.isActionInProgress)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isActionInProgress)
        assertEquals("\"Oppenheimer\" added to list!", viewModel.uiState.value.feedbackMessage)
    }

    @Test
    fun `addMediaToSharedList reverts UI state when repository fails`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SL-FAIL1",
            title = "Failure Test",
            ownerUserId = "u1",
            ownerName = "O",
            items = emptyList()
        )
        every { mockSecretSharedListRepository.observeSecretSharedList("SL-FAIL1") } returns flowOf(
            sampleList
        )
        coEvery {
            mockSecretSharedListRepository.addMediaToSharedList("SL-FAIL1", any())
        } returns Result.Error(Failure.CoreFailure.NetworkFailure)

        viewModel.init("SL-FAIL1")
        advanceUntilIdle()

        val itemToAdd = SecretSharedListItem(
            mediaId = 55,
            mediaType = MediaType.Movie,
            title = "Oppenheimer",
            posterImageUrl = "/oppenheimer.jpg"
        )

        viewModel.addMediaToSharedList(itemToAdd)

        // Immediately present in state
        assertEquals(1, viewModel.uiState.value.secretSharedList?.items?.size)

        advanceUntilIdle()

        // Rolled back to empty
        assertEquals(0, viewModel.uiState.value.secretSharedList?.items?.size)
        assertFalse(viewModel.uiState.value.isActionInProgress)
        assertEquals("Failed to sync changes. Reverted.", viewModel.uiState.value.feedbackMessage)
    }

    @Test
    fun `removeMediaFromSharedList reverts UI state when repository fails`() = runTest {
        val existingItem = SecretSharedListItem(
            mediaId = 77,
            mediaType = MediaType.Movie,
            title = "Dune 2",
            posterImageUrl = "/dune2.jpg"
        )
        val sampleList = SecretSharedList(
            shareCode = "SL-FAIL2",
            title = "Failure Test 2",
            ownerUserId = "u1",
            ownerName = "O",
            items = listOf(existingItem)
        )
        every { mockSecretSharedListRepository.observeSecretSharedList("SL-FAIL2") } returns flowOf(
            sampleList
        )
        coEvery {
            mockSecretSharedListRepository.removeMediaFromSharedList("SL-FAIL2", 77)
        } returns Result.Error(Failure.CoreFailure.NetworkFailure)

        viewModel.init("SL-FAIL2")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.secretSharedList?.items?.size)

        viewModel.removeMediaFromSharedList(77)

        // Immediately removed optimistically
        assertEquals(0, viewModel.uiState.value.secretSharedList?.items?.size)

        advanceUntilIdle()

        // Rolled back
        assertEquals(1, viewModel.uiState.value.secretSharedList?.items?.size)
        assertEquals("Dune 2", viewModel.uiState.value.secretSharedList?.items?.first()?.title)
        assertFalse(viewModel.uiState.value.isActionInProgress)
        assertEquals("Failed to sync changes. Reverted.", viewModel.uiState.value.feedbackMessage)
    }

    @Test
    fun `revokeSecretShare revokes repository and notifies caller`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SL-REV1",
            title = "Revoke Test",
            ownerUserId = "device-user-123",
            ownerName = "Me"
        )
        every { mockSecretSharedListRepository.observeSecretSharedList("SL-REV1") } returns flowOf(
            sampleList
        )

        viewModel.init("SL-REV1")
        advanceUntilIdle()

        var onRevokedCalled = false
        viewModel.revokeSecretShare { onRevokedCalled = true }
        advanceUntilIdle()

        coVerify { mockSecretSharedListRepository.revokeSecretShare("SL-REV1") }
        assertTrue(onRevokedCalled)
        assertTrue(viewModel.uiState.value.isRevoked)
    }

    @Test
    fun `init normalizes un-prefixed 4-digit shareCode to SL-XXXX`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SL-4821",
            title = "Test Normalization",
            ownerUserId = "u1",
            ownerName = "O"
        )
        every { mockSecretSharedListRepository.observeSecretSharedList("SL-4821") } returns flowOf(
            sampleList
        )

        viewModel.init("4821")
        advanceUntilIdle()

        coVerify { mockSecretSharedListRepository.observeSecretSharedList("SL-4821") }
        assertEquals(sampleList, viewModel.uiState.value.secretSharedList)
    }

    @Test
    fun `init bookmarks joined list via libraryRepository saveJoinedSecretList when non-owner opens valid list`() =
        runTest {
            val sampleList = SecretSharedList(
                shareCode = "SL-JOIN1",
                title = "Friend's Top Picks",
                description = "Best movies",
                ownerUserId = "different-user-999",
                ownerName = "Alex",
                isCollaborative = true,
                items = listOf(
                    SecretSharedListItem(
                        mediaId = 1,
                        mediaType = MediaType.Movie,
                        title = "Movie 1",
                        posterImageUrl = "/m1.jpg"
                    )
                )
            )
            every { mockSecretSharedListRepository.observeSecretSharedList("SL-JOIN1") } returns flowOf(
                sampleList
            )

            viewModel.init("SL-JOIN1")
            advanceUntilIdle()

            coVerify(exactly = 1) {
                mockLibraryRepository.saveJoinedSecretList(
                    shareCode = "SL-JOIN1",
                    title = "Friend's Top Picks",
                    description = "Best movies",
                    ownerName = "Alex",
                    coverImageUrl = "/m1.jpg",
                    itemCount = 1,
                    isCollaborative = true
                )
            }
        }

    @Test
    fun `toggleCollaborativeMode calls updateCollaborativeStatus and updates feedbackMessage`() =
        runTest {
            val sampleList = SecretSharedList(
                shareCode = "SL-COLLAB1",
                title = "Collab Test",
                ownerUserId = "device-user-123",
                ownerName = "Me",
                isCollaborative = false
            )
            every { mockSecretSharedListRepository.observeSecretSharedList("SL-COLLAB1") } returns flowOf(
                sampleList
            )

            viewModel.init("SL-COLLAB1")
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isCollaborative)

            viewModel.toggleCollaborativeMode()
            advanceUntilIdle()

            coVerify {
                mockSecretSharedListRepository.updateCollaborativeStatus(
                    "SL-COLLAB1",
                    true
                )
            }
            assertEquals(
                "Friends can now add titles to this list",
                viewModel.uiState.value.feedbackMessage
            )
        }

    @Test
    fun `leaveSharedList calls libraryRepository removeJoinedSecretList and triggers onRemoved callback`() =
        runTest {
            val sampleList = SecretSharedList(
                shareCode = "SL-LEAVE1",
                title = "Leave Test",
                ownerUserId = "other-user",
                ownerName = "Other"
            )
            every { mockSecretSharedListRepository.observeSecretSharedList("SL-LEAVE1") } returns flowOf(
                sampleList
            )

            viewModel.init("SL-LEAVE1")
            advanceUntilIdle()

            var onRemovedCalled = false
            viewModel.leaveSharedList { onRemovedCalled = true }
            advanceUntilIdle()

            coVerify { mockLibraryRepository.removeJoinedSecretList("SL-LEAVE1") }
            assertTrue(onRemovedCalled)
        }

    @Test
    fun `searchMedia with filter applies movie or tv filtering`() = runTest {
        val suggestionMovie = RemoteMultiSearchSuggestion(
            mediaType = "movie",
            id = 1,
            name = "Inception",
            popularity = 50f,
            profilePath = null,
            department = null,
            gender = 0,
            backdropPath = null,
            posterPath = "/incept.jpg",
            overview = "Dream heist",
            videoAvailable = false,
            voteAvg = 8.8f,
            voteCount = 1000,
            originalLanguage = "en",
            releaseDate = "2010-07-16",
            firstAirDate = null
        )
        val suggestionTv = RemoteMultiSearchSuggestion(
            mediaType = "tv",
            id = 2,
            name = "Breaking Bad",
            popularity = 60f,
            profilePath = null,
            department = null,
            gender = 0,
            backdropPath = null,
            posterPath = "/bb.jpg",
            overview = "Teacher cooks",
            videoAvailable = false,
            voteAvg = 9.5f,
            voteCount = 2000,
            originalLanguage = "en",
            releaseDate = null,
            firstAirDate = "2008-01-20"
        )
        val pagedPayload = PagedPayload(
            id = 0,
            page = 1,
            pageCount = 1,
            resultCount = 2,
            results = listOf(suggestionMovie, suggestionTv)
        )
        coEvery { mockTmdbApiService.multiSearch("query") } returns ApiResponse.Success(
            body = pagedPayload,
            payload = mockk(relaxed = true)
        )

        viewModel.searchMedia("query", ChallengeMediaTypeFilter.MOVIE)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.searchResults.size)
        assertEquals(1, viewModel.uiState.value.searchResults.first().mediaId)
        assertEquals(MediaType.Movie, viewModel.uiState.value.searchResults.first().mediaType)

        viewModel.searchMedia("query", ChallengeMediaTypeFilter.TV)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.searchResults.size)
        assertEquals(2, viewModel.uiState.value.searchResults.first().mediaId)
        assertEquals(MediaType.Tv, viewModel.uiState.value.searchResults.first().mediaType)
    }

    @Test
    fun `clearSearch resets query and search results`() = runTest {
        viewModel.clearSearch()

        assertEquals("", viewModel.uiState.value.searchQuery)
        assertTrue(viewModel.uiState.value.searchResults.isEmpty())
        assertFalse(viewModel.uiState.value.isSearching)
    }

    @Test
    fun `updateUserDisplayName updates state and persists to preferences`() = runTest {
        val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor

        viewModel.updateUserDisplayName("Shashank")

        assertEquals("Shashank", viewModel.uiState.value.currentUserName)
        io.mockk.verify {
            mockEditor.putString("user_display_name", "Shashank")
            mockEditor.apply()
        }
    }
}
