package com.ssverma.showtime.domain.model

import com.ssverma.showtime.api.convertToFullTmdbImageUrl
import com.ssverma.showtime.data.remote.response.RemoteReview
import com.ssverma.showtime.data.remote.response.RemoteReviewAuthor
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatLocally
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

fun RemoteReviewAuthor.asAuthor(): ReviewAuthor {
    return ReviewAuthor(
        name = name,
        userName = userName ?: "",
        avatarImageUrl = avatarPath.convertToFullTmdbImageUrl(),
        rating = rating
    )
}

private fun absentAuthor() =
    ReviewAuthor(name = "", userName = "", avatarImageUrl = "", rating = 0f)

fun RemoteReview.asReview(): Review {
    return Review(
        id = id ?: "",
        author = author?.asAuthor() ?: absentAuthor(),
        content = content ?: "",
        displayCreatedAt = DateUtils.parseIsoOffsetDateTime(createdAt)?.toLocalDate()?.formatLocally(),
        displayUpdatedAt = DateUtils.parseIsoOffsetDateTime(updatedAt)?.toLocalDate()?.formatLocally(),
        isEdited = createdAt != updatedAt
    )
}

suspend fun List<RemoteReview>.asReviews() = withContext(Dispatchers.Default) {
    map { it.asReview() }
}