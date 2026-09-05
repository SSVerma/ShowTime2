package com.ssverma.feature.library.ui.backlog.detail

import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeProgress
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.usecase.challenge.GetBacklogChallengesUseCase
import com.ssverma.shared.domain.usecase.challenge.ManageChallengeUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class ChallengeDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getBacklogChallengesUseCase: GetBacklogChallengesUseCase = mockk(relaxed = true)
    private val manageChallengeUseCase: ManageChallengeUseCase = mockk(relaxed = true)
    private val saveDiaryEntryUseCase: SaveDiaryEntryUseCase = mockk(relaxed = true)

    private val detailFlow = MutableStateFlow<Pair<ChallengeProgress?, Boolean>>(Pair(null, false))
    private lateinit var viewModel: ChallengeDetailViewModel

    private val sampleChallenge = CinephileChallenge(
        id = "top_classics",
        title = "AFI Top 100",
        description = "Greatest movies ever",
        category = ChallengeCategory.Curated,
        targetCount = 100
    )

    private val sampleProgress = ChallengeProgress(
        challenge = sampleChallenge,
        totalCount = 100,
        watchedCount = 25,
        progressPercentage = 25,
        isCompleted = false,
        watchedItems = emptyList(),
        remainingItems = emptyList(),
        milestoneTitle = "Silver Cinephile 🥈"
    )

    @Before
    fun setUp() {
        every { getBacklogChallengesUseCase.getChallengeDetailFlow("top_classics") } returns detailFlow

        viewModel = ChallengeDetailViewModel(
            getBacklogChallengesUseCase = getBacklogChallengesUseCase,
            manageChallengeUseCase = manageChallengeUseCase,
            saveDiaryEntryUseCase = saveDiaryEntryUseCase
        )
    }

    @Test
    fun `initChallenge loads challenge progress and joined status`() = runTest {
        detailFlow.value = Pair(sampleProgress, true)

        viewModel.initChallenge("top_classics")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.progress)
        assertEquals("AFI Top 100", state.progress?.challenge?.title)
        assertTrue(state.isJoined)
        assertFalse(state.isLoading)
    }

    @Test
    fun `selectFilter updates selected filter index`() = runTest {
        viewModel.selectFilter(2)
        val state = viewModel.uiState.first()
        assertEquals(2, state.selectedFilterIndex)
    }

    @Test
    fun `join confirmation workflow works properly`() = runTest {
        detailFlow.value = Pair(sampleProgress, false)
        viewModel.initChallenge("top_classics")
        advanceUntilIdle()

        viewModel.requestJoinConfirmation()
        var state = viewModel.uiState.first()
        assertTrue(state.showJoinConfirmation)

        viewModel.confirmJoin()
        advanceUntilIdle()

        coVerify(exactly = 1) { manageChallengeUseCase.joinChallenge(sampleChallenge) }
        state = viewModel.uiState.first()
        assertFalse(state.showJoinConfirmation)
    }

    @Test
    fun `leave confirmation workflow works properly`() = runTest {
        detailFlow.value = Pair(sampleProgress, true)
        viewModel.initChallenge("top_classics")
        advanceUntilIdle()

        viewModel.requestLeaveConfirmation()
        var state = viewModel.uiState.first()
        assertTrue(state.showLeaveConfirmation)

        var leftCallbackInvoked = false
        viewModel.confirmLeave { leftCallbackInvoked = true }
        advanceUntilIdle()

        coVerify(exactly = 1) { manageChallengeUseCase.leaveChallenge("top_classics") }
        assertTrue(leftCallbackInvoked)
        state = viewModel.uiState.first()
        assertFalse(state.showLeaveConfirmation)
    }

    @Test
    fun `logging dialog and saving diary entry`() = runTest {
        val mediaItem = ChallengeMediaItem(
            id = 238,
            title = "The Godfather",
            mediaType = MediaType.Movie,
            posterImageUrl = "/godfather.jpg",
            releaseYear = "1972"
        )

        viewModel.openLogDialog(mediaItem)
        var state = viewModel.uiState.first()
        assertNotNull(state.mediaItemToLog)
        assertEquals("The Godfather", state.mediaItemToLog?.title)

        val entry = mockk<DiaryEntry>(relaxed = true)
        viewModel.saveDiaryEntry(entry)
        advanceUntilIdle()

        coVerify(exactly = 1) { saveDiaryEntryUseCase(entry) }
        state = viewModel.uiState.first()
        assertNull(state.mediaItemToLog)
    }

    @Test
    fun `generateShareableChallengeText formats progress string correctly`() {
        val text = viewModel.generateShareableChallengeText(sampleProgress)
        assertTrue(text.contains("AFI Top 100"))
        assertTrue(text.contains("25/100"))
        assertTrue(text.contains("25%"))
        assertTrue(text.contains("Silver Cinephile"))
    }
}
