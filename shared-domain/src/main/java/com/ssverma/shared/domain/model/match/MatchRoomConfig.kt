package com.ssverma.shared.domain.model.match

import kotlinx.serialization.Serializable

@Serializable
enum class MatchDeckType {
    TRENDING,
    MY_SUBSCRIPTIONS,
    GENRE
}

@Serializable
data class MatchRoomConfig(
    val deckType: MatchDeckType = MatchDeckType.TRENDING,
    val genreId: Int? = null,
    val genreName: String? = null,
    val deckSize: Int = 25,
    val mode: MatchMode = MatchMode.COUCH
)
