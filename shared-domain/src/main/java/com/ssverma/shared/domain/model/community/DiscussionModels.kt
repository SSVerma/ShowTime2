package com.ssverma.shared.domain.model.community

import com.ssverma.shared.domain.model.MediaType

data class DiscussionTarget(
    val mediaType: MediaType,
    val mediaId: Int,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null
) {
    companion object {
        fun movie(movieId: Int): DiscussionTarget = DiscussionTarget(
            mediaType = MediaType.Movie,
            mediaId = movieId
        )

        fun tvShow(tvShowId: Int): DiscussionTarget = DiscussionTarget(
            mediaType = MediaType.Tv,
            mediaId = tvShowId
        )

        fun tvEpisode(tvShowId: Int, seasonNumber: Int, episodeNumber: Int): DiscussionTarget =
            DiscussionTarget(
                mediaType = MediaType.Tv,
                mediaId = tvShowId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber
            )
    }
}

data class PostCommentParams(
    val target: DiscussionTarget,
    val content: String,
    val isSpoiler: Boolean,
    val parentId: String? = null,
    val replyToAuthorName: String? = null,
    val mediaTitle: String? = null,
    val posterImageUrl: String? = null,
    val backdropImageUrl: String? = null
)

data class EditCommentParams(
    val target: DiscussionTarget,
    val commentId: String,
    val newContent: String,
    val isSpoiler: Boolean
)

data class ReportCommentParams(
    val target: DiscussionTarget,
    val commentId: String,
    val reason: String
)

data class ToggleCommentUpvoteParams(
    val target: DiscussionTarget,
    val commentId: String
)

data class DeleteCommentParams(
    val target: DiscussionTarget,
    val commentId: String
)
