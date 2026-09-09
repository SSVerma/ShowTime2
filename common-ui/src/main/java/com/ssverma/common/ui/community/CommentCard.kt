package com.ssverma.common.ui.community

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.community.Comment

@Composable
fun CommentCard(
    comment: Comment,
    onUpvoteClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onReportClick: (() -> Unit)? = null,
    onReplyClick: (() -> Unit)? = null,
    isRepliesExpanded: Boolean = true,
    onToggleRepliesExpand: (() -> Unit)? = null,
    isNestedReply: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiModel = comment.toUiModel(context = context)

    Surface(
        shape = RoundedCornerShape(size = if (isNestedReply) 12.dp else 16.dp),
        color = if (isNestedReply) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        tonalElevation = if (isNestedReply) 0.dp else 1.dp,
        modifier = modifier
    ) {
        DiscussionCommentRow(
            comment = uiModel,
            onUpvoteClick = onUpvoteClick,
            onReplyClick = onReplyClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReportClick = onReportClick,
            isRepliesExpanded = isRepliesExpanded,
            onToggleRepliesExpand = onToggleRepliesExpand,
            isNestedReply = isNestedReply,
            modifier = Modifier.padding(all = if (isNestedReply) 10.dp else 14.dp)
        )
    }
}
