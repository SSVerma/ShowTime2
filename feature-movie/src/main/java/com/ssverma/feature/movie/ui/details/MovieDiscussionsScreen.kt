package com.ssverma.feature.movie.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ssverma.shared.ui.component.community.DiscussionsScreenContent

@Composable
fun MovieDiscussionsScreen(
    viewModel: MovieDiscussionsViewModel,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discussions by viewModel.discussions.collectAsState()

    DiscussionsScreenContent(
        mediaTitle = viewModel.title,
        discussions = discussions,
        currentUserId = null,
        onBackPressed = onBackPressed,
        onPostComment = { args ->
            viewModel.postComment(
                content = args.content,
                isSpoiler = args.isSpoiler,
                parentId = args.parentId,
                replyToAuthorName = args.replyToAuthor
            )
        },
        onToggleUpvote = { commentId ->
            viewModel.toggleCommentUpvote(commentId)
        },
        onEditComment = { args ->
            viewModel.editComment(
                commentId = args.commentId,
                newContent = args.newContent,
                isSpoiler = args.isSpoiler
            )
        },
        onReportComment = { commentId, reason ->
            viewModel.reportComment(commentId, reason)
        },
        onDeleteComment = { commentId ->
            viewModel.deleteComment(commentId)
        },
        modifier = modifier
    )
}
