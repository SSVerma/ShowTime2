package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.ThreadFilter
import javax.inject.Inject

class FilterAndSortCommentsUseCase @Inject constructor() {

    operator fun invoke(
        comments: List<Comment>,
        filter: ThreadFilter = ThreadFilter.ALL,
        excludedCommentIds: Set<String> = emptySet()
    ): List<Comment> {
        val unflagged = comments.filter { it.id !in excludedCommentIds }
        val baseList = when (filter) {
            ThreadFilter.ALL -> unflagged.sortedByDescending { it.createdAtEpochMs }
            ThreadFilter.SPOILER_FREE -> unflagged.filter { !it.isSpoiler }
                .sortedByDescending { it.createdAtEpochMs }

            ThreadFilter.TOP_UPVOTED -> unflagged.sortedWith(
                compareByDescending<Comment> { it.upvotesCount }
                    .thenByDescending { it.createdAtEpochMs }
            )
        }

        return baseList.map { root ->
            val unflaggedReplies = root.replies.filter { it.id !in excludedCommentIds }
            val sortedReplies = if (filter == ThreadFilter.TOP_UPVOTED) {
                unflaggedReplies.sortedWith(
                    compareByDescending<Comment> { it.upvotesCount }
                        .thenByDescending { it.createdAtEpochMs }
                )
            } else {
                unflaggedReplies.sortedBy { it.createdAtEpochMs }
            }
            root.copy(replies = sortedReplies)
        }
    }
}
