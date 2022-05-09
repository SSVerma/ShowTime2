package com.ssverma.showtime.domain.model

data class Review(
    val id: String,
    val author: ReviewAuthor,
    val content: String,
    val displayCreatedAt: String?,
    val displayUpdatedAt: String?,
    val isEdited: Boolean
)

data class ReviewAuthor(
    val name: String?,
    val userName: String,
    val avatarImageUrl: String,
    val rating: Float
)