package com.ssverma.feature.movie.ui.game

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.model.game.DailyCinemaPuzzle
import com.ssverma.shared.domain.model.game.GameClue
import com.ssverma.shared.domain.model.game.GameClueType
import com.ssverma.shared.domain.repository.CinemaGameRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CinemaGameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: CinemaGameRepository = mockk(relaxed = true)
    private val mockTmdbService: TmdbApiService = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)

    private val samplePuzzle = DailyCinemaPuzzle(
        puzzleNumber = 42,
        epochDay = 20000L,
        targetMovieId = 27205,
        targetMovieTitle = "Inception",
        releaseYear = "2010",
        director = "Christopher Nolan",
        leadCast = listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt"),
        tagline = "Your mind is the scene of the crime.",
        synopsis = "A thief who steals corporate secrets...",
        posterImageUrl = "https://image.tmdb.org/poster.jpg",
        backdropImageUrl = "https://image.tmdb.org/backdrop.jpg",
        clues = listOf(
            GameClue(1, GameClueType.BLURRED_SHOT, "Visual Mystery", "Clue 1", "https://img1.jpg"),
            GameClue(2, GameClueType.SCENE_STILL, "Scene Still", "Clue 2", "https://img2.jpg"),
            GameClue(3, GameClueType.RELEASE_YEAR, "Release Era", "Premiered in 2010", null),
            GameClue(4, GameClueType.CAST_DIRECTOR, "Cast & Crew", "Directed by Nolan", null),
            GameClue(5, GameClueType.PLOT_TAGLINE, "Plot Tagline", "Your mind...", null)
        )
    )

    private val sampleBonusPuzzle = samplePuzzle.copy(
        targetMovieTitle = "Pulp Fiction",
        releaseYear = "1994"
    )

    private val statsFlow =
        MutableStateFlow(CinemaGameStats(currentStreak = 3, gamesPlayed = 5, gamesWon = 4))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepository.getTodayAttemptNumber() } returns 1
        coEvery { mockRepository.getTodayPuzzle(1) } returns samplePuzzle
        coEvery { mockRepository.getTodayPuzzle(2) } returns sampleBonusPuzzle
        coEvery { mockRepository.getGameStats() } returns statsFlow.value
        coEvery { mockRepository.gameStatsFlow } returns statsFlow
        coEvery { mockRepository.getTodaySubmittedGuesses() } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads puzzle and initial stats into uiState`() = runTest {
        val viewModel = CinemaGameViewModel(mockRepository, mockTmdbService, mockRewardedAdManager)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.puzzle?.targetMovieTitle).isEqualTo("Inception")
        assertThat(state.gameStatus).isEqualTo(GameStatus.IN_PROGRESS)
        assertThat(state.unlockedClueIndex).isEqualTo(0)
        assertThat(state.attemptNumber).isEqualTo(1)
        assertThat(state.isBonusReel).isFalse()
    }

    @Test
    fun `submitGuess with correct answer marks game as WON and unlocks all clues`() = runTest {
        coEvery {
            mockRepository.recordGameResult(
                isWin = true,
                guessCount = 1
            )
        } returns CinemaGameStats(
            currentStreak = 4,
            gamesPlayed = 6,
            gamesWon = 5
        )

        val viewModel = CinemaGameViewModel(mockRepository, mockTmdbService, mockRewardedAdManager)
        advanceUntilIdle()

        viewModel.submitGuess("Inception")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.gameStatus).isEqualTo(GameStatus.WON)
        assertThat(state.unlockedClueIndex).isEqualTo(4)
        assertThat(state.submittedGuesses).contains("Inception")
        assertThat(state.shareableText).isNotNull()
        assertThat(state.shareableText).contains("ShowTime Cinema Challenge #42")
        assertThat(state.shareableText).contains("🟩")
        coVerify { mockRepository.recordGameResult(isWin = true, guessCount = 1) }
    }

    @Test
    fun `submitGuess with lower-case, punctuation or article prefix matches correctly`() = runTest {
        coEvery {
            mockRepository.recordGameResult(
                isWin = true,
                guessCount = 1
            )
        } returns CinemaGameStats(
            currentStreak = 4,
            gamesPlayed = 6,
            gamesWon = 5
        )

        val viewModel = CinemaGameViewModel(mockRepository, mockTmdbService, mockRewardedAdManager)
        advanceUntilIdle()

        viewModel.submitGuess("  the inception!  ")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.gameStatus).isEqualTo(GameStatus.WON)
        assertThat(state.unlockedClueIndex).isEqualTo(4)
    }

    @Test
    fun `submitGuess with wrong answer advances clue and keeps game in progress`() = runTest {
        val viewModel = CinemaGameViewModel(mockRepository, mockTmdbService, mockRewardedAdManager)
        advanceUntilIdle()

        viewModel.submitGuess("Interstellar")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.gameStatus).isEqualTo(GameStatus.IN_PROGRESS)
        assertThat(state.unlockedClueIndex).isEqualTo(1)
        assertThat(state.selectedClueIndex).isEqualTo(1)
        assertThat(state.submittedGuesses).containsExactly("Interstellar")
    }

    @Test
    fun `unlockSecondChanceBonusReel displays non skippable rewarded ad and loads bonus puzzle for Attempt #2`() =
        runTest {
            val callbackSlot = slot<() -> Unit>()
            every {
                mockRewardedAdManager.showRewardedAdIfReady(
                    any(),
                    capture(callbackSlot)
                )
            } answers {
                callbackSlot.captured.invoke()
            }

            val viewModel =
                CinemaGameViewModel(mockRepository, mockTmdbService, mockRewardedAdManager)
            advanceUntilIdle()

            val mockActivity: Activity = mockk(relaxed = true)
            viewModel.unlockSecondChanceBonusReel(mockActivity)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.attemptNumber).isEqualTo(2)
            assertThat(state.isBonusReel).isTrue()
            assertThat(state.showBonusReelScratch).isTrue()
            assertThat(state.puzzle?.targetMovieTitle).isEqualTo("Pulp Fiction")
            assertThat(state.gameStatus).isEqualTo(GameStatus.IN_PROGRESS)
            assertThat(state.submittedGuesses).isEmpty()
            coVerify { mockRepository.saveTodayAttemptNumber(2) }
        }

    @Test
    fun `skipClue records skipped guess and unlocks next clue`() = runTest {
        val viewModel = CinemaGameViewModel(mockRepository, mockTmdbService, mockRewardedAdManager)
        advanceUntilIdle()

        viewModel.skipClue()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.submittedGuesses).containsExactly("Skipped")
        assertThat(state.unlockedClueIndex).isEqualTo(1)
    }

    @Test
    fun `resetDailyGame clears repository and reloads initial state`() = runTest {
        val viewModel = CinemaGameViewModel(mockRepository, mockTmdbService, mockRewardedAdManager)
        advanceUntilIdle()

        viewModel.resetDailyGame()
        advanceUntilIdle()

        coVerify { mockRepository.resetGameData() }
    }
}
