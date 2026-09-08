package com.ssverma.shared.domain.usecase.challenge

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.repository.BacklogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    suspend fun updateCustomChallenge(challenge: CinephileChallenge) {
        backlogRepository.updateCustomChallenge(challenge)
    }

    suspend fun addTitlesToCustomChallenge(
        challengeId: String,
        newItems: List<ChallengeMediaItem>
    ) {
        val currentChallenges = backlogRepository.activeChallengesFlow.first()
        val target = currentChallenges.firstOrNull { it.id == challengeId && it.isCustom } ?: return
        val existingKeys = target.targetMediaItems.map { it.id to it.mediaType }.toSet()
        val itemsToAdd = newItems.filterNot { existingKeys.contains(it.id to it.mediaType) }
        if (itemsToAdd.isEmpty()) return

        val updatedItems = target.targetMediaItems + itemsToAdd
        val updatedChallenge = target.copy(
            targetMediaItems = updatedItems,
            targetCount = updatedItems.size
        )
        backlogRepository.updateCustomChallenge(updatedChallenge)
    }

    suspend fun removeTitleFromCustomChallenge(
        challengeId: String,
        mediaId: Int,
        mediaType: MediaType
    ) {
        val currentChallenges = backlogRepository.activeChallengesFlow.first()
        val target = currentChallenges.firstOrNull { it.id == challengeId && it.isCustom } ?: return
        val updatedItems =
            target.targetMediaItems.filterNot { it.id == mediaId && it.mediaType == mediaType }
        val updatedChallenge = target.copy(
            targetMediaItems = updatedItems,
            targetCount = maxOf(updatedItems.size, 1)
        )
        backlogRepository.updateCustomChallenge(updatedChallenge)
    }

    suspend fun editCustomChallengeMetadata(
        challengeId: String,
        title: String,
        description: String
    ) {
        val currentChallenges = backlogRepository.activeChallengesFlow.first()
        val target = currentChallenges.firstOrNull { it.id == challengeId && it.isCustom } ?: return
        val updatedChallenge = target.copy(
            title = title.trim(),
            description = description.trim()
        )
        backlogRepository.updateCustomChallenge(updatedChallenge)
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
