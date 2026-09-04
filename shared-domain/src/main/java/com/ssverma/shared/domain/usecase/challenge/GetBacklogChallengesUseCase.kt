package com.ssverma.shared.domain.usecase.challenge

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.ChallengeProgress
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.repository.BacklogRepository
import com.ssverma.shared.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetBacklogChallengesUseCase @Inject constructor(
    private val backlogRepository: BacklogRepository,
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(): Flow<List<ChallengeProgress>> {
        return combine(
            backlogRepository.activeChallengesFlow,
            diaryRepository.getAllDiaryEntries()
        ) { activeChallenges, diaryEntries ->
            val watchedMediaSet = diaryEntries.map { it.mediaId to it.mediaType }.toSet()

            activeChallenges.map { challenge ->
                computeProgress(challenge, watchedMediaSet, diaryEntries)
            }
        }
    }

    fun getCuratedChallengesFlow(): Flow<List<ChallengeProgress>> {
        return backlogRepository.curatedChallengesFlow.map { curated ->
            curated.map { challenge ->
                computeProgress(challenge, emptySet(), emptyList())
            }
        }
    }

    suspend fun getCuratedChallengesWithProgress(forceRefresh: Boolean = false): List<ChallengeProgress> {
        val curated = backlogRepository.getCuratedChallenges(forceRefresh)
        return curated.map { challenge ->
            computeProgress(challenge, emptySet(), emptyList())
        }
    }

    fun getChallengeDetailFlow(challengeId: String): Flow<Pair<ChallengeProgress?, Boolean>> {
        return combine(
            backlogRepository.activeChallengesFlow,
            backlogRepository.curatedChallengesFlow,
            diaryRepository.getAllDiaryEntries()
        ) { activeChallenges, curatedChallenges, diaryEntries ->
            val activeChallenge = activeChallenges.firstOrNull { it.id == challengeId }
            val isJoined = activeChallenge != null
            val challenge =
                activeChallenge ?: curatedChallenges.firstOrNull { it.id == challengeId }

            if (challenge != null) {
                val watchedMediaSet = diaryEntries.map { it.mediaId to it.mediaType }.toSet()
                val progress = computeProgress(challenge, watchedMediaSet, diaryEntries)
                progress to isJoined
            } else {
                null to false
            }
        }
    }

    private fun computeProgress(
        challenge: CinephileChallenge,
        watchedMediaSet: Set<Pair<Int, MediaType>>,
        diaryEntries: List<DiaryEntry>
    ): ChallengeProgress {
        return if (challenge.targetMediaItems.isNotEmpty()) {
            val watchedItems = mutableListOf<ChallengeMediaItem>()
            val remainingItems = mutableListOf<ChallengeMediaItem>()

            challenge.targetMediaItems.forEach { item ->
                if (watchedMediaSet.contains(item.id to item.mediaType)) {
                    watchedItems.add(item)
                } else {
                    remainingItems.add(item)
                }
            }

            val totalCount = challenge.targetMediaItems.size
            val watchedCount = watchedItems.size
            val progressPercentage = if (totalCount > 0) {
                ((watchedCount.toFloat() / totalCount) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            val isCompleted = watchedCount >= totalCount && totalCount > 0
            val milestoneTitle = calculateMilestoneTitle(progressPercentage, isCompleted)

            ChallengeProgress(
                challenge = challenge,
                totalCount = totalCount,
                watchedCount = watchedCount,
                progressPercentage = progressPercentage,
                isCompleted = isCompleted,
                watchedItems = watchedItems,
                remainingItems = remainingItems,
                milestoneTitle = milestoneTitle
            )
        } else {
            // Count-based / Sprint challenge (e.g. "Watch 52 titles")
            val filteredEntries = diaryEntries.filter { entry ->
                when (challenge.mediaTypeFilter) {
                    ChallengeMediaTypeFilter.ALL -> true
                    ChallengeMediaTypeFilter.MOVIE -> entry.mediaType == MediaType.Movie
                    ChallengeMediaTypeFilter.TV -> entry.mediaType == MediaType.Tv
                }
            }

            val watchedCount = filteredEntries.size
            val totalCount = maxOf(challenge.targetCount, 1)
            val progressPercentage =
                ((watchedCount.toFloat() / totalCount) * 100).toInt().coerceIn(0, 100)
            val isCompleted = watchedCount >= totalCount
            val milestoneTitle = calculateMilestoneTitle(progressPercentage, isCompleted)

            ChallengeProgress(
                challenge = challenge,
                totalCount = totalCount,
                watchedCount = watchedCount,
                progressPercentage = progressPercentage,
                isCompleted = isCompleted,
                watchedItems = emptyList(),
                remainingItems = emptyList(),
                milestoneTitle = milestoneTitle
            )
        }
    }

    private fun calculateMilestoneTitle(percentage: Int, isCompleted: Boolean): String {
        return when {
            isCompleted || percentage >= 100 -> "Master Cinephile 🏆"
            percentage >= 75 -> "Gold Maestro 🥇"
            percentage >= 50 -> "Silver Connoisseur 🥈"
            percentage >= 25 -> "Bronze Scholar 🥉"
            else -> "Cinephile Initiate 🌱"
        }
    }
}
