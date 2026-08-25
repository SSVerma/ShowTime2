package com.ssverma.feature.movie.ui.game

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.ui.component.GameParticleType
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.model.game.DailyCinemaPuzzle
import com.ssverma.shared.domain.repository.CinemaGameRepository
import com.ssverma.shared.domain.utils.AppConfigConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class CinemaGameViewModel @Inject constructor(
    private val cinemaGameRepository: CinemaGameRepository,
    private val tmdbApiService: TmdbApiService,
    private val rewardedAdManager: RewardedAdManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CinemaGameUiState())
    val uiState: StateFlow<CinemaGameUiState> = _uiState.asStateFlow()

    private val _gameEffectEvent = Channel<Pair<GameParticleType, Long>>(Channel.BUFFERED)
    val gameEffectEvent: Flow<Pair<GameParticleType, Long>> = _gameEffectEvent.receiveAsFlow()

    private var searchJob: Job? = null

    init {
        loadDailyPuzzleAndStats()
        rewardedAdManager.loadAd()
    }

    private fun loadDailyPuzzleAndStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val attemptNumber = cinemaGameRepository.getTodayAttemptNumber()
            val puzzle = cinemaGameRepository.getTodayPuzzle(attemptNumber)
            val stats = cinemaGameRepository.getGameStats()
            val savedGuesses = cinemaGameRepository.getTodaySubmittedGuesses()

            var status = GameStatus.IN_PROGRESS
            var unlockedIndex = 0

            if (savedGuesses.isNotEmpty()) {
                val hasWon = savedGuesses.any { isMatchingTitle(it, puzzle.targetMovieTitle) }
                if (hasWon) {
                    status = GameStatus.WON
                    unlockedIndex = 4
                } else if (savedGuesses.size >= 5) {
                    status = GameStatus.LOST
                    unlockedIndex = 4
                } else {
                    unlockedIndex = savedGuesses.size.coerceAtMost(4)
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    puzzle = puzzle,
                    stats = stats,
                    attemptNumber = attemptNumber,
                    submittedGuesses = savedGuesses,
                    unlockedClueIndex = unlockedIndex,
                    selectedClueIndex = if (status != GameStatus.IN_PROGRESS) 0 else unlockedIndex,
                    gameStatus = status,
                    shareableText = if (status != GameStatus.IN_PROGRESS) {
                        buildShareableText(
                            puzzle,
                            stats,
                            savedGuesses,
                            status == GameStatus.WON,
                            attemptNumber > 1
                        )
                    } else null
                )
            }

            // Continuously observe stats flow
            launch {
                cinemaGameRepository.gameStatsFlow.collectLatest { updatedStats ->
                    _uiState.update { it.copy(stats = updatedStats) }
                }
            }
        }
    }

    fun selectClue(index: Int) {
        val maxUnlocked = _uiState.value.unlockedClueIndex
        if (index in 0..maxUnlocked) {
            _uiState.update { it.copy(selectedClueIndex = index) }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _uiState.update { it.copy(searchSuggestions = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(250) // Debounce typing
            _uiState.update { it.copy(isSearching = true) }

            when (val response = tmdbApiService.multiSearch(query = query.trim())) {
                is ApiResponse.Success -> {
                    val mediaSuggestions = response.body.results?.filter {
                        it.mediaType.equals("movie", ignoreCase = true)
                    }?.distinctBy { it.id }.orEmpty().take(5)

                    _uiState.update {
                        it.copy(
                            searchSuggestions = mediaSuggestions,
                            isSearching = false
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            searchSuggestions = emptyList(),
                            isSearching = false
                        )
                    }
                }
            }
        }
    }

    fun submitGuess(guessedTitle: String) {
        val state = _uiState.value
        val puzzle = state.puzzle ?: return
        if (state.isGameOver) return

        val cleanGuess = guessedTitle.trim()
        if (cleanGuess.isBlank()) return

        viewModelScope.launch {
            val updatedGuesses = state.submittedGuesses + cleanGuess
            val isCorrect = isMatchingTitle(cleanGuess, puzzle.targetMovieTitle)

            val newStatus: GameStatus
            val newUnlockedIndex: Int

            if (isCorrect) {
                newStatus = GameStatus.WON
                newUnlockedIndex = 4
                val updatedStats = cinemaGameRepository.recordGameResult(
                    isWin = true,
                    guessCount = updatedGuesses.size
                )
                val shareText = buildShareableText(
                    puzzle,
                    updatedStats,
                    updatedGuesses,
                    isWin = true,
                    state.isBonusReel
                )
                _uiState.update {
                    it.copy(
                        submittedGuesses = updatedGuesses,
                        gameStatus = newStatus,
                        unlockedClueIndex = newUnlockedIndex,
                        selectedClueIndex = newUnlockedIndex,
                        searchQuery = "",
                        searchSuggestions = emptyList(),
                        stats = updatedStats,
                        shareableText = shareText
                    )
                }
                _gameEffectEvent.send(GameParticleType.SUCCESS_CONFETTI to System.currentTimeMillis())
                delay(700)
                _uiState.update { it.copy(showStatsDialog = true) }
            } else if (updatedGuesses.size >= 5) {
                newStatus = GameStatus.LOST
                newUnlockedIndex = 4
                val updatedStats = cinemaGameRepository.recordGameResult(
                    isWin = false,
                    guessCount = 5
                )
                val shareText = buildShareableText(
                    puzzle,
                    updatedStats,
                    updatedGuesses,
                    isWin = false,
                    state.isBonusReel
                )
                _uiState.update {
                    it.copy(
                        submittedGuesses = updatedGuesses,
                        gameStatus = newStatus,
                        unlockedClueIndex = newUnlockedIndex,
                        selectedClueIndex = newUnlockedIndex,
                        searchQuery = "",
                        searchSuggestions = emptyList(),
                        stats = updatedStats,
                        shareableText = shareText
                    )
                }
                _gameEffectEvent.send(GameParticleType.GAME_OVER_LOST to System.currentTimeMillis())
                rewardedAdManager.loadAd() // Preload non-skippable rewarded ad

                // Only auto-open the stats dialog if it was already Attempt #2 (Bonus Reel).
                // On Attempt #1, stay on the game screen so the Second Chance CTA is front and center!
                if (state.attemptNumber > 1) {
                    delay(700)
                    _uiState.update { it.copy(showStatsDialog = true) }
                }
            } else {
                newStatus = GameStatus.IN_PROGRESS
                newUnlockedIndex = updatedGuesses.size.coerceAtMost(4)
                _uiState.update {
                    it.copy(
                        submittedGuesses = updatedGuesses,
                        unlockedClueIndex = newUnlockedIndex,
                        selectedClueIndex = newUnlockedIndex,
                        searchQuery = "",
                        searchSuggestions = emptyList()
                    )
                }
                _gameEffectEvent.send(GameParticleType.WRONG_GUESS_EMBER to System.currentTimeMillis())
            }

            cinemaGameRepository.saveTodaySubmittedGuesses(updatedGuesses)
        }
    }

    fun unlockSecondChanceBonusReel(activity: Activity) {
        _uiState.update { it.copy(isUnlockingSecondChance = true) }
        rewardedAdManager.showRewardedAdIfReady(activity) {
            viewModelScope.launch {
                val newAttempt = 2
                cinemaGameRepository.saveTodayAttemptNumber(newAttempt)
                cinemaGameRepository.saveTodaySubmittedGuesses(emptyList())

                val bonusPuzzle = cinemaGameRepository.getTodayPuzzle(newAttempt)
                val stats = cinemaGameRepository.getGameStats()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isUnlockingSecondChance = false,
                        showBonusReelScratch = true,
                        puzzle = bonusPuzzle,
                        stats = stats,
                        attemptNumber = newAttempt,
                        submittedGuesses = emptyList(),
                        unlockedClueIndex = 0,
                        selectedClueIndex = 0,
                        gameStatus = GameStatus.IN_PROGRESS,
                        showStatsDialog = false,
                        shareableText = null,
                        searchQuery = "",
                        searchSuggestions = emptyList()
                    )
                }
                _gameEffectEvent.send(
                    GameParticleType.SUCCESS_CONFETTI to System.currentTimeMillis()
                )
            }
        }
    }

    fun onBonusReelScratched() {
        _uiState.update { it.copy(showBonusReelScratch = false) }
        viewModelScope.launch {
            _gameEffectEvent.send(
                GameParticleType.SUCCESS_CONFETTI to System.currentTimeMillis()
            )
        }
    }

    fun skipClue() {
        submitGuess("Skipped")
    }

    fun setShowStatsDialog(show: Boolean) {
        _uiState.update { it.copy(showStatsDialog = show) }
    }

    private fun isMatchingTitle(guess: String, target: String): Boolean {
        val normalizedGuess = normalizeTitle(guess)
        val normalizedTarget = normalizeTitle(target)
        if (normalizedGuess == normalizedTarget) return true

        val stripArticles = { s: String ->
            s.removePrefix("the").removePrefix("a").removePrefix("an")
        }
        return stripArticles(normalizedGuess) == stripArticles(normalizedTarget)
    }

    private fun normalizeTitle(title: String): String {
        val withoutAccents = Normalizer.normalize(title, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
        return withoutAccents.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }

    private fun buildShareableText(
        puzzle: DailyCinemaPuzzle,
        stats: CinemaGameStats,
        guesses: List<String>,
        isWin: Boolean,
        isBonusReel: Boolean = false
    ): String {
        val sb = StringBuilder()
        val bonusTag = if (isBonusReel) " (Bonus Reel)" else ""

        val wittyComment = when {
            isWin && isBonusReel -> "BONUS REEL HERO - Streak Saved"
            isWin && guesses.size == 1 -> "CINEMA CLAIRVOYANT - Nailed it on Clue #1!"
            isWin && guesses.size == 2 -> "DIRECTOR'S EYE - 2/5 Clues"
            isWin && guesses.size == 3 -> "FILM BUFF - 3/5 Clues"
            isWin && guesses.size == 4 -> "BOX OFFICE CLUTCH - 4/5 Clues"
            isWin && guesses.size == 5 -> "POST-CREDITS MIRACLE - 5/5 Clues"
            else -> "ROLLED THE CREDITS - Better luck tomorrow"
        }

        sb.append("ShowTime Cinema Challenge #${puzzle.puzzleNumber}$bonusTag\n")
        sb.append("$wittyComment\n\n")

        val score = if (isWin) "${guesses.size}/5" else "X/5"
        sb.append("Score: $score | Streak: ${stats.currentStreak} Days\n\n")

        val clueLabels = listOf("Frame", "Still", "Era", "Cast", "Plot")
        for (i in 0 until 5) {
            val label = clueLabels.getOrElse(i) { "Clue" }
            when {
                i < guesses.size -> {
                    if (isMatchingTitle(guesses[i], puzzle.targetMovieTitle)) {
                        sb.append("$label: 🟩\n")
                    } else if (guesses[i].equals("Skipped", ignoreCase = true)) {
                        sb.append("$label: ⬛ (Skipped)\n")
                    } else {
                        sb.append("$label: 🟥\n")
                    }
                }

                else -> sb.append("$label: ⬜\n")
            }
        }

        sb.append("\nCan you beat my movie IQ?\n")
        sb.append("Play daily on ShowTime: ${AppConfigConstants.PLAY_STORE_URL}")
        return sb.toString()
    }

    fun resetDailyGame() {
        viewModelScope.launch {
            cinemaGameRepository.resetGameData()
            loadDailyPuzzleAndStats()
        }
    }
}
