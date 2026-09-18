package com.ssverma.shared.domain.model.game

import com.google.gson.annotations.SerializedName

data class CinemaGameStats(
    @SerializedName("currentStreak")
    val currentStreak: Int = 0,
    @SerializedName("maxStreak")
    val maxStreak: Int = 0,
    @SerializedName("gamesPlayed")
    val gamesPlayed: Int = 0,
    @SerializedName("gamesWon")
    val gamesWon: Int = 0,
    @SerializedName("lastPlayedEpochDay")
    val lastPlayedEpochDay: Long = -1L,
    @SerializedName("lastWonEpochDay")
    val lastWonEpochDay: Long = -1L,
    @SerializedName("guessDistribution")
    val guessDistribution: Map<Int, Int> = mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
) {
    val winPercentage: Int
        get() = if (gamesPlayed == 0) 0 else ((gamesWon.toDouble() / gamesPlayed) * 100).toInt()
}

enum class GameClueType {
    @SerializedName("BLURRED_SHOT")
    BLURRED_SHOT,

    @SerializedName("SCENE_STILL")
    SCENE_STILL,

    @SerializedName("RELEASE_YEAR")
    RELEASE_YEAR,

    @SerializedName("CAST_DIRECTOR")
    CAST_DIRECTOR,

    @SerializedName("PLOT_TAGLINE")
    PLOT_TAGLINE
}

data class GameClue(
    @SerializedName("clueNumber")
    val clueNumber: Int,
    @SerializedName("type")
    val type: GameClueType,
    @SerializedName("label")
    val label: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("imageUrl")
    val imageUrl: String? = null
)

data class DailyCinemaPuzzle(
    @SerializedName("puzzleNumber")
    val puzzleNumber: Int,
    @SerializedName("epochDay")
    val epochDay: Long,
    @SerializedName("targetMovieId")
    val targetMovieId: Int,
    @SerializedName("targetMovieTitle")
    val targetMovieTitle: String,
    @SerializedName("releaseYear")
    val releaseYear: String,
    @SerializedName("director")
    val director: String,
    @SerializedName("leadCast")
    val leadCast: List<String>,
    @SerializedName("tagline")
    val tagline: String,
    @SerializedName("synopsis")
    val synopsis: String,
    @SerializedName("posterImageUrl")
    val posterImageUrl: String,
    @SerializedName("backdropImageUrl")
    val backdropImageUrl: String,
    @SerializedName("clues")
    val clues: List<GameClue>
)
