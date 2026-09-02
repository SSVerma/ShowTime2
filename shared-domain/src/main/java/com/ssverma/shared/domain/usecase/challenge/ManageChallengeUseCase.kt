package com.ssverma.shared.domain.usecase.challenge

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.repository.BacklogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageChallengeUseCase @Inject constructor(
    private val backlogRepository: BacklogRepository
) {
    val blindspotsFlow: Flow<List<BlindspotPriorityItem>>
        get() = backlogRepository.blindspotsFlow

    suspend fun joinChallenge(challenge: CinephileChallenge) {
        backlogRepository.joinChallenge(challenge)
    }

    suspend fun leaveChallenge(challengeId: String) {
        backlogRepository.leaveChallenge(challengeId)
    }

    suspend fun createCustomChallenge(
        title: String,
        description: String,
        mediaTypeFilter: ChallengeMediaTypeFilter,
        targetCount: Int,
        targetItems: List<ChallengeMediaItem> = emptyList()
    ): CinephileChallenge {
        return backlogRepository.createCustomChallenge(
            title = title,
            description = description,
            mediaTypeFilter = mediaTypeFilter,
            targetCount = targetCount,
            targetItems = targetItems
        )
    }

    suspend fun deleteCustomChallenge(challengeId: String) {
        backlogRepository.deleteCustomChallenge(challengeId)
    }

    suspend fun addBlindspot(item: BlindspotPriorityItem) {
        backlogRepository.addBlindspot(item)
    }

    suspend fun removeBlindspot(mediaId: Int, mediaType: MediaType) {
        backlogRepository.removeBlindspot(mediaId, mediaType)
    }

    suspend fun isBlindspot(mediaId: Int, mediaType: MediaType): Boolean {
        return backlogRepository.isBlindspot(mediaId, mediaType)
    }
}
