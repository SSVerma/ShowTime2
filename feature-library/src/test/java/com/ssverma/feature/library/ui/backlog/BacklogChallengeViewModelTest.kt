package com.ssverma.feature.library.ui.backlog

import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.ChallengeProgress
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.repository.BacklogRepository
import com.ssverma.shared.domain.repository.DiaryRepository
import com.ssverma.shared.domain.usecase.challenge.GetBacklogChallengesUseCase
import com.ssverma.shared.domain.usecase.challenge.ManageChallengeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BacklogChallengeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val backlogRepository: BacklogRepository = mockk(relaxed = true)
    private val diaryRepository: DiaryRepository = mockk(relaxed = true)

    private val activeChallengesFlow = MutableStateFlow<List<CinephileChallenge>>(emptyList())
    private val blindspotsFlow = MutableStateFlow<List<BlindspotPriorityItem>>(emptyList())
    private val diaryEntriesFlow = MutableStateFlow<List<DiaryEntry>>(emptyList())

    private lateinit var getBacklogChallengesUseCase: GetBacklogChallengesUseCase
    private lateinit var manageChallengeUseCase: ManageChallengeUseCase
    private lateinit var viewModel: BacklogChallengeViewModel

    @Before
    fun setUp() {
        every { backlogRepository.activeChallengesFlow } returns activeChallengesFlow
        every { backlogRepository.blindspotsFlow } returns blindspotsFlow
        every { diaryRepository.getAllDiaryEntries() } returns diaryEntriesFlow

        getBacklogChallengesUseCase = GetBacklogChallengesUseCase(
            backlogRepository = backlogRepository,
            diaryRepository = diaryRepository
        )
        manageChallengeUseCase = ManageChallengeUseCase(
            backlogRepository = backlogRepository
        )

        viewModel = BacklogChallengeViewModel(
            getBacklogChallengesUseCase = getBacklogChallengesUseCase,
            manageChallengeUseCase = manageChallengeUseCase
        )
    }

    @Test
    fun `initial state collects active challenges and blindspots`() = runTest {
        val sampleMovie = ChallengeMediaItem(
            id = 238,
            title = "The Godfather",
            mediaType = MediaType.Movie,
            posterImageUrl = "/godfather.jpg",
            releaseYear = "1972"
        )
        val sampleChallenge = CinephileChallenge(
            id = "c1",
            title = "Classics",
            description = "Top classics",
            category = ChallengeCategory.Curated,
            targetCount = 1,
            targetMediaItems = listOf(sampleMovie)
        )

        activeChallengesFlow.value = listOf(sampleChallenge)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(1, state.activeChallenges.size)
        assertEquals("Classics", state.activeChallenges.first().challenge.title)
        assertEquals(0, state.activeChallenges.first().watchedCount)
        assertEquals(1, state.activeChallenges.first().remainingItems.size)
    }

    @Test
    fun `joining challenge and opening details works properly`() = runTest {
        val sampleChallenge = CinephileChallenge(
            id = "c2",
            title = "Prestige TV",
            description = "Iconic shows",
            category = ChallengeCategory.Curated,
            targetCount = 5
        )

        viewModel.joinCuratedChallenge(sampleChallenge)
        advanceUntilIdle()

        coVerify(exactly = 1) { backlogRepository.joinChallenge(sampleChallenge) }

        val dummyProgress = ChallengeProgress(
            challenge = sampleChallenge,
            totalCount = 5,
            watchedCount = 2,
            progressPercentage = 40,
            isCompleted = false,
            watchedItems = emptyList(),
            remainingItems = emptyList(),
            milestoneTitle = "Bronze Scholar 🥉"
        )

        viewModel.openChallengeDetail(dummyProgress)
        val stateWithDetail = viewModel.uiState.first()
        assertNotNull(stateWithDetail.selectedChallengeDetail)
        assertEquals("Prestige TV", stateWithDetail.selectedChallengeDetail?.challenge?.title)

        val shareText = viewModel.generateShareableChallengeText(dummyProgress)
        assertTrue(shareText.contains("Prestige TV"))
        assertTrue(shareText.contains("40%"))
    }

    @Test
    fun `creating custom goal invokes repository`() = runTest {
        viewModel.createCustomGoal(
            title = "2026 Marathon",
            description = "50 titles",
            mediaTypeFilter = ChallengeMediaTypeFilter.ALL,
            targetCount = 50
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            backlogRepository.createCustomChallenge(
                title = "2026 Marathon",
                description = "50 titles",
                mediaTypeFilter = ChallengeMediaTypeFilter.ALL,
                targetCount = 50,
                targetItems = any()
            )
        }
    }
}
