package com.ssverma.feature.library.ui.share

import android.content.Context
import android.content.SharedPreferences
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.SecretSharedList
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.repository.SecretSharedListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
    val mainDispatcherRule = MainDispatcherRule()

    private val mockSecretSharedListRepository: SecretSharedListRepository = mockk(relaxed = true)
    private val mockLibraryRepository: LibraryRepository = mockk(relaxed = true)
    private val mockTmdbApiService: TmdbApiService = mockk(relaxed = true)
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

        viewModel = SecretSharedListViewModel(
            secretSharedListRepository = mockSecretSharedListRepository,
            libraryRepository = mockLibraryRepository,
            tmdbApiService = mockTmdbApiService,
            context = mockContext
        )
    }

    @Test
    fun `init loads and observes secret shared list`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SECRET999",
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

        every { mockSecretSharedListRepository.observeSecretSharedList("SECRET999") } returns flowOf(
            sampleList
        )

        viewModel.init("SECRET999")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleList, state.secretSharedList)
        assertTrue(state.isOwner)
        assertTrue(state.isCollaborative)
        assertFalse(state.isRevoked)
    }

    @Test
    fun `addAllToWatchlist invokes library repository for items not yet in watchlist`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "CODE1",
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

        every { mockSecretSharedListRepository.observeSecretSharedList("CODE1") } returns flowOf(
            sampleList
        )
        viewModel.init("CODE1")
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
        assertEquals("All titles added to your Watchlist!", viewModel.uiState.value.feedbackMessage)
    }

    @Test
    fun `cloneToMyLists creates custom list and adds all media`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "CLONE1",
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

        every { mockSecretSharedListRepository.observeSecretSharedList("CLONE1") } returns flowOf(
            sampleList
        )
        coEvery {
            mockLibraryRepository.createCustomList(
                any(),
                any(),
                any()
            )
        } returns "custom-list-42"

        viewModel.init("CLONE1")
        advanceUntilIdle()

        viewModel.cloneToMyLists()
        advanceUntilIdle()

        coVerify {
            mockLibraryRepository.createCustomList("Cyberpunk Gems", "Neon sci-fi", "/br.jpg")
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
    }

    @Test
    fun `addMediaToSharedList and removeMediaFromSharedList call repository`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "SHARE1",
            title = "Test",
            ownerUserId = "u1",
            ownerName = "O"
        )
        every { mockSecretSharedListRepository.observeSecretSharedList("SHARE1") } returns flowOf(
            sampleList
        )

        viewModel.init("SHARE1")
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

        coVerify { mockSecretSharedListRepository.addMediaToSharedList("SHARE1", itemToAdd) }
        assertEquals("\"Dune\" added to list!", viewModel.uiState.value.feedbackMessage)

        viewModel.removeMediaFromSharedList(99)
        advanceUntilIdle()

        coVerify { mockSecretSharedListRepository.removeMediaFromSharedList("SHARE1", 99) }
    }

    @Test
    fun `revokeSecretShare revokes repository and notifies caller`() = runTest {
        val sampleList = SecretSharedList(
            shareCode = "REV1",
            title = "Revoke Test",
            ownerUserId = "device-user-123",
            ownerName = "Me"
        )
        every { mockSecretSharedListRepository.observeSecretSharedList("REV1") } returns flowOf(
            sampleList
        )

        viewModel.init("REV1")
        advanceUntilIdle()

        var onRevokedCalled = false
        viewModel.revokeSecretShare { onRevokedCalled = true }
        advanceUntilIdle()

        coVerify { mockSecretSharedListRepository.revokeSecretShare("REV1") }
        assertTrue(onRevokedCalled)
        assertTrue(viewModel.uiState.value.isRevoked)
    }
}
