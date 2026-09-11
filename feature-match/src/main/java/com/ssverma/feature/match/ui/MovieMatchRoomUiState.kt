package com.ssverma.feature.match.ui

import com.ssverma.core.ui.UiText
import com.ssverma.shared.domain.model.match.MatchMode
import com.ssverma.shared.domain.model.match.MatchRoomConfig
import com.ssverma.shared.domain.model.match.MovieMatchCard

enum class MatchScreenPhase {
    SETUP,
    JOIN,
    SWIPING,
    HANDOFF,
    SUMMARY
}

data class MovieMatchRoomUiState(
    val phase: MatchScreenPhase = MatchScreenPhase.SETUP,
    val mode: MatchMode = MatchMode.COUCH,
    val config: MatchRoomConfig = MatchRoomConfig(),
    val player1Name: String = "Player 1",
    val player2Name: String = "Player 2",
    val activePlayerIndex: Int = 0,
    val isHost: Boolean = true,
    val roomCode: String? = null,
    val joinCodeInput: String = "",
    val cards: List<MovieMatchCard> = emptyList(),
    val topCardIndex: Int = 0,
    val player1Likes: Set<Int> = emptySet(),
    val player2Likes: Set<Int> = emptySet(),
    val swipeHistory: List<Pair<Int, Boolean>> = emptyList(),
    val matches: List<MovieMatchCard> = emptyList(),
    val celebratingMatch: MovieMatchCard? = null,
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val isProOrPassActive: Boolean = false,
    val showQuotaModal: Boolean = false,
    val showSetupSheet: Boolean = false,
    val showSynopsisSheet: MovieMatchCard? = null,
    val savedWatchlistIds: Set<Int> = emptySet(),
    val isGuestConnected: Boolean = false
) {
    val activePlayerName: String
        get() = if (activePlayerIndex == 0) player1Name else player2Name

    val partnerName: String
        get() = when {
            (mode == MatchMode.REMOTE || config.mode == MatchMode.REMOTE) && isHost -> player2Name.ifBlank { "Partner" }
            (mode == MatchMode.REMOTE || config.mode == MatchMode.REMOTE) && !isHost -> player1Name.ifBlank { "Host" }
            activePlayerIndex == 0 -> player2Name.ifBlank { "Player 2" }
            else -> player1Name.ifBlank { "Player 1" }
        }

    val currentCard: MovieMatchCard?
        get() = cards.getOrNull(topCardIndex)

    val nextCard: MovieMatchCard?
        get() = cards.getOrNull(topCardIndex + 1)

    val remainingCardsCount: Int
        get() = (cards.size - topCardIndex).coerceAtLeast(0)

    val canUndo: Boolean
        get() = isProOrPassActive && swipeHistory.isNotEmpty() && topCardIndex > 0
}
