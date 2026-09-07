package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.match.MatchRoom
import com.ssverma.shared.domain.model.match.MatchRoomConfig
import com.ssverma.shared.domain.model.match.MovieMatchCard
import com.ssverma.shared.domain.model.match.SwipeDirection
import kotlinx.coroutines.flow.Flow

interface MatchRoomRepository {
    suspend fun fetchMatchDeck(config: MatchRoomConfig): Result<List<MovieMatchCard>, Failure<*>>

    suspend fun createRemoteRoom(
        hostName: String,
        deck: List<MovieMatchCard>
    ): Result<MatchRoom, Failure<*>>

    suspend fun joinRemoteRoom(
        roomCode: String,
        guestName: String
    ): Result<MatchRoom, Failure<*>>

    fun observeRemoteRoom(roomId: String): Flow<MatchRoom?>

    suspend fun submitRemoteSwipe(
        roomId: String,
        isHost: Boolean,
        movieId: Int,
        direction: SwipeDirection
    ): Result<Unit, Failure<*>>

    suspend fun canStartMatchSession(isProActive: Boolean): Boolean

    suspend fun recordMatchSessionStarted()

    suspend fun saveMatchToWatchlist(card: MovieMatchCard): Result<Unit, Failure<*>>
}
