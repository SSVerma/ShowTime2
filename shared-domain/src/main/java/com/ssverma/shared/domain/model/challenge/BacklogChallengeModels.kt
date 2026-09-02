package com.ssverma.shared.domain.model.challenge

import com.ssverma.shared.domain.model.MediaType

enum class ChallengeCategory {
    Curated,
    DirectorSpotlight,
    DecadeClassics,
    GenreSprint,
    PersonalGoal
}

enum class ChallengeMediaTypeFilter {
    ALL,
    MOVIE,
    TV
}

data class ChallengeMediaItem(
    val id: Int,
    val title: String,
    val mediaType: MediaType,
    val posterImageUrl: String,
    val backdropImageUrl: String = "",
    val releaseYear: String,
    val directorOrCreator: String = "",
    val overview: String = "",
    val voteAvg: Float = 0f
)

data class CinephileChallenge(
    val id: String,
    val title: String,
    val description: String,
    val category: ChallengeCategory,
    val mediaTypeFilter: ChallengeMediaTypeFilter = ChallengeMediaTypeFilter.ALL,
    val targetCount: Int,
    val targetMediaItems: List<ChallengeMediaItem> = emptyList(),
    val isCustom: Boolean = false,
    val joinedAt: Long? = null,
    val completedAt: Long? = null
)

data class ChallengeProgress(
    val challenge: CinephileChallenge,
    val totalCount: Int,
    val watchedCount: Int,
    val progressPercentage: Int,
    val isCompleted: Boolean,
    val watchedItems: List<ChallengeMediaItem>,
    val remainingItems: List<ChallengeMediaItem>,
    val milestoneTitle: String
)

data class BlindspotPriorityItem(
    val mediaId: Int,
    val mediaType: MediaType,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String = "",
    val releaseYear: String,
    val voteAvg: Float = 0f,
    val priorityNote: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)
