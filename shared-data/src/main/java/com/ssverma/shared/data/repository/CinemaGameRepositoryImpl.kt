package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.shared.data.game.DailyCinemaPuzzleProvider
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.model.game.DailyCinemaPuzzle
import com.ssverma.shared.domain.repository.CinemaGameRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CinemaGameRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    keyValueStorageClient: KeyValueStorageClient,
    private val puzzleProvider: DailyCinemaPuzzleProvider
) : CinemaGameRepository {

    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "showtime_cinema_game_prefs")
    )

    private companion object {
        val KEY_CURRENT_STREAK = intPreferencesKey("game_current_streak")
        val KEY_MAX_STREAK = intPreferencesKey("game_max_streak")
        val KEY_GAMES_PLAYED = intPreferencesKey("game_games_played")
        val KEY_GAMES_WON = intPreferencesKey("game_games_won")
        val KEY_LAST_PLAYED_EPOCH = longPreferencesKey("game_last_played_epoch")
        val KEY_LAST_WON_EPOCH = longPreferencesKey("game_last_won_epoch")
        val KEY_GUESS_DIST_1 = intPreferencesKey("game_dist_1")
        val KEY_GUESS_DIST_2 = intPreferencesKey("game_dist_2")
        val KEY_GUESS_DIST_3 = intPreferencesKey("game_dist_3")
        val KEY_GUESS_DIST_4 = intPreferencesKey("game_dist_4")
        val KEY_GUESS_DIST_5 = intPreferencesKey("game_dist_5")
        val KEY_TODAY_EPOCH = longPreferencesKey("game_today_epoch")
        val KEY_TODAY_GUESSES_CSV = stringPreferencesKey("game_today_guesses_csv")
        val KEY_TODAY_ATTEMPT = intPreferencesKey("game_today_attempt")
    }

    override val gameStatsFlow: Flow<CinemaGameStats> = storage.data.map { prefs ->
        CinemaGameStats(
            currentStreak = prefs[KEY_CURRENT_STREAK] ?: 0,
            maxStreak = prefs[KEY_MAX_STREAK] ?: 0,
            gamesPlayed = prefs[KEY_GAMES_PLAYED] ?: 0,
            gamesWon = prefs[KEY_GAMES_WON] ?: 0,
            lastPlayedEpochDay = prefs[KEY_LAST_PLAYED_EPOCH] ?: -1L,
            lastWonEpochDay = prefs[KEY_LAST_WON_EPOCH] ?: -1L,
            guessDistribution = mapOf(
                1 to (prefs[KEY_GUESS_DIST_1] ?: 0),
                2 to (prefs[KEY_GUESS_DIST_2] ?: 0),
                3 to (prefs[KEY_GUESS_DIST_3] ?: 0),
                4 to (prefs[KEY_GUESS_DIST_4] ?: 0),
                5 to (prefs[KEY_GUESS_DIST_5] ?: 0)
            )
        )
    }

    override suspend fun getGameStats(): CinemaGameStats = withContext(Dispatchers.IO) {
        gameStatsFlow.first()
    }

    override suspend fun getTodayPuzzle(attemptNumber: Int): DailyCinemaPuzzle =
        withContext(Dispatchers.IO) {
            puzzleProvider.getTodayPuzzle(attemptNumber)
        }

    override suspend fun isTodayPuzzleCompleted(): Boolean = withContext(Dispatchers.IO) {
        val todayEpochDay = LocalDate.now(ZoneOffset.UTC).toEpochDay()
        val stats = getGameStats()
        stats.lastPlayedEpochDay == todayEpochDay
    }

    override suspend fun getTodayAttemptNumber(): Int = withContext(Dispatchers.IO) {
        val todayEpochDay = LocalDate.now(ZoneOffset.UTC).toEpochDay()
        val prefs = storage.data.first()
        val savedEpoch = prefs[KEY_TODAY_EPOCH] ?: -1L
        if (savedEpoch != todayEpochDay) {
            1
        } else {
            prefs[KEY_TODAY_ATTEMPT] ?: 1
        }
    }

    override suspend fun saveTodayAttemptNumber(attemptNumber: Int): Unit =
        withContext(Dispatchers.IO) {
            val todayEpochDay = LocalDate.now(ZoneOffset.UTC).toEpochDay()
            storage.edit { prefs ->
                prefs[KEY_TODAY_EPOCH] = todayEpochDay
                prefs[KEY_TODAY_ATTEMPT] = attemptNumber
            }
            Unit
        }

    override suspend fun getTodaySubmittedGuesses(): List<String> = withContext(Dispatchers.IO) {
        val todayEpochDay = LocalDate.now(ZoneOffset.UTC).toEpochDay()
        val prefs = storage.data.first()
        val savedEpoch = prefs[KEY_TODAY_EPOCH] ?: -1L
        if (savedEpoch != todayEpochDay) {
            emptyList()
        } else {
            val csv = prefs[KEY_TODAY_GUESSES_CSV].orEmpty()
            if (csv.isBlank()) emptyList() else csv.split("|||")
        }
    }

    override suspend fun saveTodaySubmittedGuesses(guesses: List<String>): Unit =
        withContext(Dispatchers.IO) {
            val todayEpochDay = LocalDate.now(ZoneOffset.UTC).toEpochDay()
            val csv = guesses.joinToString("|||")
            storage.edit { prefs ->
                prefs[KEY_TODAY_EPOCH] = todayEpochDay
                prefs[KEY_TODAY_GUESSES_CSV] = csv
            }
            Unit
        }

    override suspend fun recordGameResult(
        isWin: Boolean,
        guessCount: Int
    ): CinemaGameStats = withContext(Dispatchers.IO) {
        val todayEpochDay = LocalDate.now(ZoneOffset.UTC).toEpochDay()
        var updatedStats = getGameStats()

        storage.edit { prefs ->
            val prevPlayedEpoch = prefs[KEY_LAST_PLAYED_EPOCH] ?: -1L
            if (prevPlayedEpoch == todayEpochDay) {
                // Already recorded today
                return@edit
            }

            val currentPlayed = (prefs[KEY_GAMES_PLAYED] ?: 0) + 1
            val prevWon = prefs[KEY_GAMES_WON] ?: 0
            val prevStreak = prefs[KEY_CURRENT_STREAK] ?: 0
            val prevMaxStreak = prefs[KEY_MAX_STREAK] ?: 0
            val lastWonEpoch = prefs[KEY_LAST_WON_EPOCH] ?: -1L

            val newStreak = if (isWin) {
                if (lastWonEpoch == todayEpochDay - 1) {
                    prevStreak + 1
                } else {
                    1
                }
            } else {
                0
            }

            val newMaxStreak = maxOf(prevMaxStreak, newStreak)
            val newWon = if (isWin) prevWon + 1 else prevWon

            prefs[KEY_GAMES_PLAYED] = currentPlayed
            prefs[KEY_GAMES_WON] = newWon
            prefs[KEY_CURRENT_STREAK] = newStreak
            prefs[KEY_MAX_STREAK] = newMaxStreak
            prefs[KEY_LAST_PLAYED_EPOCH] = todayEpochDay

            if (isWin) {
                prefs[KEY_LAST_WON_EPOCH] = todayEpochDay
                when (guessCount) {
                    1 -> prefs[KEY_GUESS_DIST_1] = (prefs[KEY_GUESS_DIST_1] ?: 0) + 1
                    2 -> prefs[KEY_GUESS_DIST_2] = (prefs[KEY_GUESS_DIST_2] ?: 0) + 1
                    3 -> prefs[KEY_GUESS_DIST_3] = (prefs[KEY_GUESS_DIST_3] ?: 0) + 1
                    4 -> prefs[KEY_GUESS_DIST_4] = (prefs[KEY_GUESS_DIST_4] ?: 0) + 1
                    5 -> prefs[KEY_GUESS_DIST_5] = (prefs[KEY_GUESS_DIST_5] ?: 0) + 1
                }
            }
        }

        updatedStats = getGameStats()
        updatedStats
    }

    override suspend fun resetGameData(): Unit = withContext(Dispatchers.IO) {
        storage.edit { prefs ->
            prefs.clear()
        }
    }
}
