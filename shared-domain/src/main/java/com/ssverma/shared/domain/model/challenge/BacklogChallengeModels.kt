package com.ssverma.shared.domain.model.challenge

import com.google.gson.annotations.SerializedName
import com.ssverma.shared.domain.model.MediaType

enum class ChallengeCategory {
    @SerializedName("Curated")
    Curated,

    @SerializedName("DirectorSpotlight")
    DirectorSpotlight,

    @SerializedName("DecadeClassics")
    DecadeClassics,

    @SerializedName("GenreSprint")
    GenreSprint,

    @SerializedName("PersonalGoal")
    PersonalGoal
}

enum class ChallengeMediaTypeFilter {
    @SerializedName("ALL")
    ALL,

    @SerializedName("MOVIE")
    MOVIE,

    @SerializedName("TV")
    TV
}

data class ChallengeMediaItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("mediaType")
    val mediaType: MediaType,
    @SerializedName("posterImageUrl")
    val posterImageUrl: String,
    @SerializedName("backdropImageUrl")
    val backdropImageUrl: String = "",
    @SerializedName("releaseYear")
    val releaseYear: String,
    @SerializedName("directorOrCreator")
    val directorOrCreator: String = "",
    @SerializedName("overview")
    val overview: String = "",
    @SerializedName("voteAvg")
    val voteAvg: Float = 0f
)

data class CinephileChallenge(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("category")
    val category: ChallengeCategory,
    @SerializedName("mediaTypeFilter")
    val mediaTypeFilter: ChallengeMediaTypeFilter = ChallengeMediaTypeFilter.ALL,
    @SerializedName("targetCount")
    val targetCount: Int,
    @SerializedName("targetMediaItems")
    val targetMediaItems: List<ChallengeMediaItem> = emptyList(),
    @SerializedName("isCustom")
    val isCustom: Boolean = false,
    @SerializedName("joinedAt")
    val joinedAt: Long? = null,
    @SerializedName("completedAt")
    val completedAt: Long? = null
)

data class ChallengeProgress(
    @SerializedName("challenge")
    val challenge: CinephileChallenge,
    @SerializedName("totalCount")
    val totalCount: Int,
    @SerializedName("watchedCount")
    val watchedCount: Int,
    @SerializedName("progressPercentage")
    val progressPercentage: Int,
    @SerializedName("isCompleted")
    val isCompleted: Boolean,
    @SerializedName("watchedItems")
    val watchedItems: List<ChallengeMediaItem>,
    @SerializedName("remainingItems")
    val remainingItems: List<ChallengeMediaItem>,
    @SerializedName("milestoneTitle")
    val milestoneTitle: String
)

data class BlindspotPriorityItem(
    @SerializedName("mediaId")
    val mediaId: Int,
    @SerializedName("mediaType")
    val mediaType: MediaType,
    @SerializedName("title")
    val title: String,
    @SerializedName("posterImageUrl")
    val posterImageUrl: String,
    @SerializedName("backdropImageUrl")
    val backdropImageUrl: String = "",
    @SerializedName("releaseYear")
    val releaseYear: String,
    @SerializedName("voteAvg")
    val voteAvg: Float = 0f,
    @SerializedName("priorityNote")
    val priorityNote: String? = null,
    @SerializedName("addedAt")
    val addedAt: Long = System.currentTimeMillis()
)
