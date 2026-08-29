package com.ssverma.shared.domain.model.community

data class Comment(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val content: String,
    val isSpoiler: Boolean = false,
    val upvotesCount: Int = 0,
    val isUpvotedByMe: Boolean = false,
    val isOwner: Boolean = false,
    val isEdited: Boolean = false,
    val parentId: String? = null,
    val replyToAuthorName: String? = null,
    val repliesCount: Int = 0,
    val replies: List<Comment> = emptyList(),
    val reportCount: Int = 0,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
