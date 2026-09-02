package com.ssverma.feature.library.ui.wrapped

import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.usecase.stats.GetCinephileWrappedUseCase
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.testing.fakes.FakeDiaryRepository
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CinephileWrappedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDiaryRepository: FakeDiaryRepository
    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var viewModel: CinephileWrappedViewModel

    @Before
    fun setUp() {
        fakeDiaryRepository = FakeDiaryRepository()
        fakeLibraryRepository = FakeLibraryRepository()

        val getCinephileWrappedUseCase = GetCinephileWrappedUseCase(
            diaryRepository = fakeDiaryRepository,
            libraryRepository = fakeLibraryRepository
        )

        viewModel = CinephileWrappedViewModel(
            getCinephileWrappedUseCase = getCinephileWrappedUseCase
        )
    }

    @Test
    fun `initial state loads all-time summary`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.isLoading)
        assertNotNull(state.summary)
        assertEquals(0, state.selectedYear)
        assertEquals(0, state.summary!!.totalLogged)
    }

    @Test
    fun `selecting year updates state and generates share text`() = runTest {
        val date2026 = DateUtils.toMillis(LocalDate.of(2026, 6, 1))

        fakeDiaryRepository.saveDiaryEntry(
            DiaryEntry(
                id = 1,
                mediaId = 1,
                mediaType = MediaType.Movie,
                title = "Dune: Part Two",
                posterImageUrl = "/dune2.jpg",
                releaseDate = "2024-03-01",
                userRating = 5.0f,
                isRewatch = false,
                loggedAt = date2026
            )
        )

        viewModel.onSelectYear(2026)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(2026, state.selectedYear)
        assertEquals(1, state.summary?.totalLogged)
        assertEquals("Dune: Part Two", state.summary?.topRatedMedia?.firstOrNull()?.title)

        val shareText = viewModel.generateWrappedShareText(state.summary!!)
        assertTrue(shareText.contains("Dune: Part Two"))
        assertTrue(shareText.contains("ShowTime"))
    }

    @Test
    fun `selecting milestone updates selectedMilestone state`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.first()
        val firstMilestone = state.summary?.milestones?.firstOrNull()
        assertNotNull(firstMilestone)

        viewModel.onSelectMilestone(firstMilestone)
        advanceUntilIdle()

        val updatedState = viewModel.uiState.first()
        assertEquals(firstMilestone, updatedState.selectedMilestone)

        viewModel.onSelectMilestone(null)
        advanceUntilIdle()

        val clearedState = viewModel.uiState.first()
        assertEquals(null, clearedState.selectedMilestone)
    }
}
