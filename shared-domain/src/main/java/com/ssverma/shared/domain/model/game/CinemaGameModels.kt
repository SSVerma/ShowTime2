package com.ssverma.shared.domain.model.game

data class CinemaGameStats(
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val lastPlayedEpochDay: Long = -1L,
    val lastWonEpochDay: Long = -1L,
    val guessDistribution: Map<Int, Int> = mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
) {
    val winPercentage: Int
        get() = if (gamesPlayed == 0) 0 else ((gamesWon.toDouble() / gamesPlayed) * 100).toInt()
}

enum class GameClueType {
    BLURRED_SHOT,
    SCENE_STILL,
    RELEASE_YEAR,
    CAST_DIRECTOR,
    PLOT_TAGLINE
}

data class GameClue(
    val clueNumber: Int,
    val type: GameClueType,
    val label: String,
    val content: String,
    val imageUrl: String? = null
)

data class DailyCinemaPuzzle(
    val puzzleNumber: Int,
    val epochDay: Long,
    val targetMovieId: Int,
    val targetMovieTitle: String,
    val releaseYear: String,
    val director: String,
    val leadCast: List<String>,
    val tagline: String,
    val synopsis: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val clues: List<GameClue>
)
