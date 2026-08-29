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
        onPostComment = { content, isSpoiler, parentId, replyToAuthorName ->
            viewModel.postComment(
                content = content,
                isSpoiler = isSpoiler,
                parentId = parentId,
                replyToAuthorName = replyToAuthorName
            )
        },
        onToggleUpvote = { commentId ->
            viewModel.toggleCommentUpvote(commentId)
        },
        onEditComment = { commentId, newContent, isSpoiler ->
            viewModel.editComment(commentId, newContent, isSpoiler)
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
