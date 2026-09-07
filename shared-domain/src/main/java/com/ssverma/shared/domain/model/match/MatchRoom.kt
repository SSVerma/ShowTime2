package com.ssverma.shared.domain.model.match

import kotlinx.serialization.Serializable

@Serializable
enum class MatchMode {
    COUCH,
    REMOTE
}

@Serializable
enum class MatchRoomStatus {
    WAITING_FOR_GUEST,
    SWIPING,
    COMPLETED
}

@Serializable
enum class SwipeDirection {
    LIKE,
    PASS
}

@Serializable
data class MatchRoom(
    val id: String,
    val roomCode: String,
    val hostUserId: String,
    val hostName: String,
    val guestUserId: String? = null,
    val guestName: String? = null,
    val mode: MatchMode = MatchMode.REMOTE,
    val status: MatchRoomStatus = MatchRoomStatus.WAITING_FOR_GUEST,
    val deckCards: List<MovieMatchCard> = emptyList(),
    val hostLikes: List<Int> = emptyList(),
    val hostPasses: List<Int> = emptyList(),
    val guestLikes: List<Int> = emptyList(),
    val guestPasses: List<Int> = emptyList(),
    val matches: List<Int> = emptyList(),
    val createdAtEpochMs: Long = System.currentTimeMillis()
) {
    val isMatched: Boolean get() = matches.isNotEmpty()

    fun matchedCards(): List<MovieMatchCard> {
        val matchSet = matches.toSet()
        return deckCards.filter { matchSet.contains(it.id) }
    }
}
