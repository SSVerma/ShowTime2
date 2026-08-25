package com.ssverma.feature.movie.ui.game

import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion
import com.ssverma.core.ui.UiText
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.model.game.DailyCinemaPuzzle

enum class GameStatus {
    IN_PROGRESS,
    WON,
    LOST
}

data class CinemaGameUiState(
    val isLoading: Boolean = true,
    val puzzle: DailyCinemaPuzzle? = null,
    val unlockedClueIndex: Int = 0,
    val selectedClueIndex: Int = 0,
    val submittedGuesses: List<String> = emptyList(),
    val searchQuery: String = "",
    val searchSuggestions: List<RemoteMultiSearchSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    val gameStatus: GameStatus = GameStatus.IN_PROGRESS,
    val stats: CinemaGameStats = CinemaGameStats(),
    val showStatsDialog: Boolean = false,
    val shareableText: String? = null,
    val attemptNumber: Int = 1,
    val isUnlockingSecondChance: Boolean = false,
    val showBonusReelScratch: Boolean = false,
    val errorMessage: UiText? = null
) {
    val remainingGuesses: Int
        get() = (5 - submittedGuesses.size).coerceAtLeast(0)

    val isGameOver: Boolean
        get() = gameStatus != GameStatus.IN_PROGRESS

    val isBonusReel: Boolean
        get() = attemptNumber > 1

    val canUnlockSecondChance: Boolean
        get() = gameStatus == GameStatus.LOST && attemptNumber == 1
}
