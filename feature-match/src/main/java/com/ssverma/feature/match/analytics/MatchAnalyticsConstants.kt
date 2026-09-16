package com.ssverma.feature.match.analytics

object MatchAnalyticsKeys {
    const val MODE = "mode"
    const val ROOM_CODE = "room_code"
    const val CARDS_COUNT = "cards_count"
    const val DIRECTION = "direction"
    const val TOTAL_MATCHES = "total_matches"
}

object MatchAnalyticsEventName {
    const val MATCH_ROOM_CREATED = "match_room_created"
    const val MATCH_ROOM_JOINED = "match_room_joined"
    const val MATCH_CARD_SWIPED = "match_card_swiped"
    const val MATCH_FOUND = "match_found"
    const val MATCH_ROOM_COMPLETED = "match_room_completed"
}
