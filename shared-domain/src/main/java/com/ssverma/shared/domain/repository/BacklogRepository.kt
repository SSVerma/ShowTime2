package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import kotlinx.coroutines.flow.Flow

interface BacklogRepository {
    val activeChallengesFlow: Flow<List<CinephileChallenge>>
    val blindspotsFlow: Flow<List<BlindspotPriorityItem>>

    suspend fun getCuratedChallenges(): List<CinephileChallenge>
    suspend fun joinChallenge(challenge: CinephileChallenge)
    suspend fun leaveChallenge(challengeId: String)
    suspend fun createCustomChallenge(
        title: String,
        description: String,
        mediaTypeFilter: ChallengeMediaTypeFilter,
        targetCount: Int,
        targetItems: List<ChallengeMediaItem>
    ): CinephileChallenge

    suspend fun deleteCustomChallenge(challengeId: String)
    suspend fun addBlindspot(item: BlindspotPriorityItem)
    suspend fun removeBlindspot(mediaId: Int, mediaType: MediaType)
    suspend fun isBlindspot(mediaId: Int, mediaType: MediaType): Boolean
}
