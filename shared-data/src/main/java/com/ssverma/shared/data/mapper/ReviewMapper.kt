package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.RemoteReview
import com.ssverma.api.service.tmdb.response.RemoteReviewAuthor
import com.ssverma.core.domain.model.Review
import com.ssverma.core.domain.model.ReviewAuthor
import com.ssverma.core.domain.utils.DateUtils
import com.ssverma.core.domain.utils.formatLocally
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReviewsMapper @Inject constructor() : ListMapper<RemoteReview, Review>() {
    override suspend fun mapItem(input: RemoteReview): Review {
        return input.asReview()
    }
}

fun RemoteReviewAuthor.asAuthor(): ReviewAuthor {
    return ReviewAuthor(
        name = name,
        userName = userName.orEmpty(),
        avatarImageUrl = avatarPath.convertToFullTmdbImageUrl(),
        rating = rating
    )
}

private fun absentAuthor() =
    ReviewAuthor(name = "", userName = "", avatarImageUrl = "", rating = 0f)

fun RemoteReview.asReview(): Review {
    return Review(
        id = id.orEmpty(),
        author = author?.asAuthor() ?: absentAuthor(),
        content = content.orEmpty(),
        displayCreatedAt = DateUtils.parseIsoOffsetDateTime(createdAt)?.toLocalDate()
            ?.formatLocally(),
        displayUpdatedAt = DateUtils.parseIsoOffsetDateTime(updatedAt)?.toLocalDate()
            ?.formatLocally(),
        isEdited = createdAt != updatedAt
    )
}

suspend fun List<RemoteReview>.asReviews() = withContext(Dispatchers.Default) {
    map { it.asReview() }
}