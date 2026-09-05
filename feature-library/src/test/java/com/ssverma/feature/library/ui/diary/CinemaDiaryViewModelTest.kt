package com.ssverma.feature.library.ui.diary

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.usecase.diary.DeleteDiaryEntryUseCase
import com.ssverma.shared.domain.usecase.diary.GetDiaryEntriesUseCase
import com.ssverma.shared.domain.usecase.diary.GetDiarySummaryStatsUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import com.ssverma.shared.testing.fakes.FakeDiaryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CinemaDiaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeDiaryRepository
    private val tmdbApiService: TmdbApiService = mockk(relaxed = true)
    private lateinit var viewModel: CinemaDiaryViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeDiaryRepository()
        viewModel = CinemaDiaryViewModel(
            getDiaryEntriesUseCase = GetDiaryEntriesUseCase(fakeRepository),
            getDiarySummaryStatsUseCase = GetDiarySummaryStatsUseCase(fakeRepository),
            saveDiaryEntryUseCase = SaveDiaryEntryUseCase(fakeRepository),
            deleteDiaryEntryUseCase = DeleteDiaryEntryUseCase(fakeRepository),
            tmdbApiService = tmdbApiService
        )
    }

    @Test
    fun `initial state is loaded and reflects repository entries and stats`() = runTest {
        val entry = DiaryEntry(
            id = 1L,
            mediaId = 101,
            mediaType = MediaType.Movie,
            title = "Dune: Part Two",
            posterImageUrl = "/dune2.jpg",
            userRating = 4.5f,
            review = "Visual feast!",
            isRewatch = false
        )
        fakeRepository.saveDiaryEntry(entry)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(1, state.totalEntriesCount)
        assertEquals(1, state.timelineGroups.size)
        assertEquals(1, state.stats.totalLogged)
        assertEquals(1, state.stats.totalMovies)
        assertEquals(0, state.stats.totalTvShows)
        assertEquals(4.5f, state.stats.averageUserRating, 0.01f)
    }

    @Test
    fun `filter switches properly between categories`() = runTest {
        fakeRepository.saveDiaryEntry(
            DiaryEntry(
                id = 1L,
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Movie A",
                posterImageUrl = "/a.jpg",
                userRating = 3.0f
            )
        )
        fakeRepository.saveDiaryEntry(
            DiaryEntry(
                id = 2L,
                mediaId = 202,
                mediaType = MediaType.Tv,
                title = "Show B",
                posterImageUrl = "/b.jpg",
                userRating = 5.0f,
                isRewatch = true
            )
        )
        advanceUntilIdle()

        viewModel.setFilter(DiaryFilterType.TV_ONLY)
        advanceUntilIdle()

        val tvState = viewModel.uiState.first()
        assertEquals(DiaryFilterType.TV_ONLY, tvState.activeFilter)
        assertEquals(1, tvState.totalEntriesCount)
        assertEquals("Show B", tvState.timelineGroups.first().entries.first().title)

        viewModel.setFilter(DiaryFilterType.REWATCHES_ONLY)
        advanceUntilIdle()

        val rewatchState = viewModel.uiState.first()
        assertEquals(1, rewatchState.totalEntriesCount)
    }

    @Test
    fun `editing and deleting dialog state flow works seamlessly`() = runTest {
        val entry = DiaryEntry(
            id = 1L,
            mediaId = 101,
            mediaType = MediaType.Movie,
            title = "Movie Test",
            posterImageUrl = "/test.jpg",
            userRating = 4.0f
        )
        fakeRepository.saveDiaryEntry(entry)
        advanceUntilIdle()

        viewModel.onEditEntry(entry)
        assertEquals(entry, viewModel.uiState.first().entryPendingEdit)

        viewModel.onSaveEditedEntry(entry.copy(userRating = 5.0f))
        advanceUntilIdle()
        assertNull(viewModel.uiState.first().entryPendingEdit)

        viewModel.onRequestDeleteEntry(entry)
        assertNotNull(viewModel.uiState.first().entryPendingDelete)

        viewModel.onConfirmDeleteEntry()
        advanceUntilIdle()
        assertNull(viewModel.uiState.first().entryPendingDelete)
        assertEquals(0, viewModel.uiState.first().totalEntriesCount)
    }

    @Test
    fun `search to log open and dismiss controls state properly`() = runTest {
        viewModel.onOpenLogSearch()
        advanceUntilIdle()

        var state = viewModel.uiState.first()
        assertTrue(state.isSearchingToLog)
        assertEquals("", state.mediaSearchQuery)
        assertTrue(state.mediaSearchSuggestions.isEmpty())
        assertFalse(state.isSearchingMedia)

        viewModel.onDismissLogSearch()
        advanceUntilIdle()

        state = viewModel.uiState.first()
        assertFalse(state.isSearchingToLog)
    }

    @Test
    fun `searching media triggers TMDB multi search and updates suggestions`() = runTest {
        val mockSuggestion = RemoteMultiSearchSuggestion(
            mediaType = "movie",
            id = 550,
            name = "Fight Club",
            popularity = 95f,
            profilePath = null,
            department = null,
            gender = 0,
            backdropPath = "/backdrop.jpg",
            posterPath = "/poster.jpg",
            overview = "An insomniac office worker...",
            videoAvailable = false,
            voteAvg = 8.4f,
            voteCount = 24000,
            originalLanguage = "en",
            releaseDate = "1999-10-15",
            firstAirDate = null
        )

        coEvery { tmdbApiService.multiSearch("Fight") } returns ApiResponse.Success(
            body = PagedPayload(
                id = 0,
                page = 1,
                pageCount = 1,
                resultCount = 1,
                results = listOf(mockSuggestion)
            ),
            payload = mockk(relaxed = true)
        )

        viewModel.onSearchQueryChange("Fight", ChallengeMediaTypeFilter.ALL)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(1, state.mediaSearchSuggestions.size)
        assertEquals("Fight Club", state.mediaSearchSuggestions.first().title)
        assertEquals(MediaType.Movie, state.mediaSearchSuggestions.first().mediaType)
        assertEquals("1999", state.mediaSearchSuggestions.first().releaseYear)
    }

    @Test
    fun `selecting media item and saving new entry persists to repository and updates timeline`() =
        runTest {
            val mediaItem = ChallengeMediaItem(
                id = 550,
                title = "Fight Club",
                mediaType = MediaType.Movie,
                posterImageUrl = "/poster.jpg",
                releaseYear = "1999",
                voteAvg = 8.4f
            )

            viewModel.onSelectMediaToLog(mediaItem)
            advanceUntilIdle()

            var state = viewModel.uiState.first()
            assertFalse(state.isSearchingToLog)
            assertEquals(mediaItem, state.mediaItemPendingLog)

            val newEntry = DiaryEntry(
                id = 100L,
                mediaId = mediaItem.id,
                mediaType = mediaItem.mediaType,
                title = mediaItem.title,
                posterImageUrl = mediaItem.posterImageUrl,
                userRating = 5.0f,
                review = "Masterpiece",
                isRewatch = true
            )

            viewModel.onSaveNewEntry(newEntry)
            advanceUntilIdle()

            state = viewModel.uiState.first()
            assertNull(state.mediaItemPendingLog)
            assertEquals(1, state.totalEntriesCount)
            assertEquals("Fight Club", state.timelineGroups.first().entries.first().title)
            assertEquals(5.0f, state.timelineGroups.first().entries.first().userRating, 0.01f)
        }
}

