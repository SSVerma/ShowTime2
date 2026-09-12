package com.ssverma.shared.domain.model.community

data class CommunityCuratedList(
    val listId: String,
    val title: String,
    val description: String? = null,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val categoryTag: String = "Cinephile Favorites",
    val itemCount: Int = 0,
    val items: List<CommunityCuratedListItem> = emptyList(),
    val previewPosters: List<String> = emptyList(),
    val upvotesCount: Long = 0L,
    val clonesCount: Long = 0L,
    val isUpvotedByMe: Boolean = false,
    val isClonedByMe: Boolean = false,
    val isMine: Boolean = false,
    val reportCount: Long = 0L,
    val isFlagged: Boolean = reportCount >= CommunityModerationConfig.DEFAULT_COMMUNITY_LISTS_FLAG_THRESHOLD,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

