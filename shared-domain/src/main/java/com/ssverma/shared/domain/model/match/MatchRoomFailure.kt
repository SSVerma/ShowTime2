package com.ssverma.shared.domain.model.match

sealed interface MatchRoomFailure {
    object CannotJoinOwnRoom : MatchRoomFailure
    object RoomNotFound : MatchRoomFailure
    object RoomFull : MatchRoomFailure
    object RoomExpired : MatchRoomFailure
    object RateLimited : MatchRoomFailure
}
