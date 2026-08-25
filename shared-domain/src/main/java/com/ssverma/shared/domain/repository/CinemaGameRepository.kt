package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.model.game.DailyCinemaPuzzle
import kotlinx.coroutines.flow.Flow

interface CinemaGameRepository {
    val gameStatsFlow: Flow<CinemaGameStats>
    suspend fun getGameStats(): CinemaGameStats
    suspend fun getTodayPuzzle(attemptNumber: Int = 1): DailyCinemaPuzzle
    suspend fun recordGameResult(isWin: Boolean, guessCount: Int): CinemaGameStats
    suspend fun getTodaySubmittedGuesses(): List<String>
    suspend fun saveTodaySubmittedGuesses(guesses: List<String>)
    suspend fun getTodayAttemptNumber(): Int
    suspend fun saveTodayAttemptNumber(attemptNumber: Int)
    suspend fun isTodayPuzzleCompleted(): Boolean
    suspend fun resetGameData()
}
