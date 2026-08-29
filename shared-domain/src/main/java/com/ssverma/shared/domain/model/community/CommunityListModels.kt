package com.ssverma.shared.domain.model.community

import com.ssverma.shared.domain.model.library.CustomList

data class PublishCustomListParams(
    val localList: CustomList,
    val categoryTag: String
)

data class UnpublishCustomListParams(
    val listId: String
)

data class ToggleListUpvoteParams(
    val listId: String
)

data class CloneCommunityListParams(
    val communityList: CommunityCuratedList
)

object CommunityListCategories {
    const val ALL = "All"
    const val MIND_BENDING = "Mind-Bending"
    const val SCI_FI = "Sci-Fi Essentials"
    const val A24 = "A24 Gems"
    const val HORROR = "Horror & Thriller"
    const val COMFORT = "Comfort Watch"
    const val DIRECTOR_SPOTLIGHT = "Director Spotlight"
    const val HIDDEN_GEMS = "Hidden Gems"
    const val AWARD_WINNERS = "Award Winners"

    val DEFAULT_CATEGORIES = listOf(
        ALL,
        MIND_BENDING,
        SCI_FI,
        A24,
        HORROR,
        COMFORT,
        DIRECTOR_SPOTLIGHT,
        HIDDEN_GEMS,
        AWARD_WINNERS
    )
}
