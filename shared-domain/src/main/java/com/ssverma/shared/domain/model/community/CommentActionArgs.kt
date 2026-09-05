package com.ssverma.shared.domain.model.community

data class PostCommentArgs(
    val content: String,
    val isSpoiler: Boolean = false,
    val parentId: String? = null,
    val replyToAuthor: String? = null
)

data class EditCommentArgs(
    val commentId: String,
    val newContent: String,
    val isSpoiler: Boolean = false
)
