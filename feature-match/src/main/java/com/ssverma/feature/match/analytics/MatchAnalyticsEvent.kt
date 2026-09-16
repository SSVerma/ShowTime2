package com.ssverma.feature.match.analytics

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class MatchAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class RoomCreated(
        val mode: String,
        val roomCode: String? = null,
        val cardsCount: Int,
        val sourceScreen: String = MatchAnalyticsScreenName.MOVIE_MATCH_ROOM
    ) : MatchAnalyticsEvent(
        eventName = MatchAnalyticsEventName.MATCH_ROOM_CREATED,
        params = mutableMapOf<String, AnalyticsParam>().apply {
            putAll(
                listOfNotNull(
                    MatchAnalyticsKeys.MODE to mode,
                    roomCode?.let { MatchAnalyticsKeys.ROOM_CODE to it },
                    MatchAnalyticsKeys.CARDS_COUNT to cardsCount,
                    SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
                )
            )
        }
    )

    data class RoomJoined(
        val roomCode: String,
        val sourceScreen: String = MatchAnalyticsScreenName.MOVIE_MATCH_ROOM
    ) : MatchAnalyticsEvent(
        eventName = MatchAnalyticsEventName.MATCH_ROOM_JOINED,
        params = mapOf(
            MatchAnalyticsKeys.ROOM_CODE to roomCode,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class CardSwiped(
        val mediaId: Int,
        val direction: String,
        val mode: String,
        val sourceScreen: String = MatchAnalyticsScreenName.MOVIE_MATCH_ROOM
    ) : MatchAnalyticsEvent(
        eventName = MatchAnalyticsEventName.MATCH_CARD_SWIPED,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            MatchAnalyticsKeys.DIRECTION to direction,
            MatchAnalyticsKeys.MODE to mode,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class MatchFound(
        val mediaId: Int,
        val mode: String,
        val totalMatches: Int,
        val sourceScreen: String = MatchAnalyticsScreenName.MOVIE_MATCH_ROOM
    ) : MatchAnalyticsEvent(
        eventName = MatchAnalyticsEventName.MATCH_FOUND,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            MatchAnalyticsKeys.MODE to mode,
            MatchAnalyticsKeys.TOTAL_MATCHES to totalMatches,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class RoomCompleted(
        val mode: String,
        val totalMatches: Int,
        val sourceScreen: String = MatchAnalyticsScreenName.MOVIE_MATCH_ROOM
    ) : MatchAnalyticsEvent(
        eventName = MatchAnalyticsEventName.MATCH_ROOM_COMPLETED,
        params = mapOf(
            MatchAnalyticsKeys.MODE to mode,
            MatchAnalyticsKeys.TOTAL_MATCHES to totalMatches,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
