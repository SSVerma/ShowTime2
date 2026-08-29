package com.ssverma.shared.ui.component.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.community.CommentCard
import com.ssverma.shared.ui.component.community.PostCommentBottomSheet
import kotlinx.coroutines.launch

private const val MaxPreviewDiscussions = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDiscussionsSection(
    discussions: List<Comment>,
    onDiscussionsViewAllClick: () -> Unit,
    onPostComment: (content: String, isSpoiler: Boolean) -> Unit,
    onEditComment: (commentId: String, newContent: String, isSpoiler: Boolean) -> Unit = { _, _, _ -> },
    onReportComment: (commentId: String, reason: String) -> Unit = { _, _ -> },
    onToggleUpvote: (commentId: String) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    modifier: Modifier = Modifier,
    currentUserId: String? = null
) {
    var isPostSheetVisible by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    var commentToReport by remember { mutableStateOf<Comment?>(null) }
    val expandedCommentIds = remember { mutableStateListOf<String>() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.discussions),
                modifier = Modifier.padding(horizontal = 16.dp),
                trailingActionLabel = stringResource(id = R.string.see_all),
                onTrailingActionClicked = onDiscussionsViewAllClick,
                hideTrailingAction = discussions.isEmpty()
            )
        },
        headerContentSpacing = 12.dp,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Interactive Thought Entry Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                onClick = {
                    if (onDiscussionsViewAllClick != null) {
                        onDiscussionsViewAllClick()
                    } else {
                        coroutineScope.launch {
                            isPostSheetVisible = true
                            sheetState.show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddComment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.post_thought_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Preview items (max 3)
            val previewComments = discussions.take(MaxPreviewDiscussions)
            previewComments.forEach { comment ->
                val isMyComment =
                    comment.isOwner || (currentUserId != null && comment.authorId == currentUserId)
                val isRepliesExpanded = comment.id in expandedCommentIds

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CommentCard(
                        comment = comment,
                        onUpvoteClick = { onToggleUpvote(comment.id) },
                        onEditClick = if (isMyComment) {
                            onDiscussionsViewAllClick
                        } else null,
                        onDeleteClick = if (isMyComment) {
                            { commentToDelete = comment }
                        } else null,
                        onReportClick = if (!isMyComment) {
                            { commentToReport = comment }
                        } else null,
                        onReplyClick = onDiscussionsViewAllClick,
                        isRepliesExpanded = isRepliesExpanded,
                        onToggleRepliesExpand = {
                            if (comment.id in expandedCommentIds) {
                                expandedCommentIds.remove(comment.id)
                            } else {
                                expandedCommentIds.add(comment.id)
                            }
                        }
                    )

                    // Nested replies preview when expanded
                    AnimatedVisibility(
                        visible = isRepliesExpanded && comment.replies.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp)
                        ) {
                            val previewReplies = comment.replies.take(2)
                            previewReplies.forEach { reply ->
                                val isMyReply =
                                    reply.isOwner || (currentUserId != null && reply.authorId == currentUserId)
                                CommentCard(
                                    comment = reply,
                                    onUpvoteClick = { onToggleUpvote(reply.id) },
                                    onEditClick = if (isMyReply) {
                                        onDiscussionsViewAllClick
                                    } else null,
                                    onDeleteClick = if (isMyReply) {
                                        { commentToDelete = reply }
                                    } else null,
                                    onReportClick = if (!isMyReply) {
                                        { commentToReport = reply }
                                    } else null,
                                    onReplyClick = onDiscussionsViewAllClick,
                                    isNestedReply = true
                                )
                            }

                            if (comment.replies.size > 2 && onDiscussionsViewAllClick != null) {
                                androidx.compose.material3.TextButton(
                                    onClick = onDiscussionsViewAllClick,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Text(
                                        text = "View all ${comment.replies.size} replies in Discussions →",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // View all button if more items exist
            if (discussions.size > MaxPreviewDiscussions) {
                OutlinedButton(
                    onClick = onDiscussionsViewAllClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.view_all_thoughts_in_thread,
                            discussions.size
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    commentToDelete?.let { comment ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { commentToDelete = null },
            title = { Text(stringResource(id = R.string.delete_thought_dialog_title)) },
            text = { Text(stringResource(id = R.string.delete_thought_dialog_msg)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onDeleteComment(comment.id)
                        commentToDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_action),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { commentToDelete = null }) {
                    Text(stringResource(id = R.string.cancel_action))
                }
            }
        )
    }

    // Report Reason Dialog
    commentToReport?.let { comment ->
        var selectedReason by remember { mutableStateOf("Spam or advertising") }
        val reasons = listOf(
            stringResource(id = R.string.report_reason_spam),
            stringResource(id = R.string.report_reason_harassment),
            stringResource(id = R.string.report_reason_spoiler),
            stringResource(id = R.string.report_reason_inappropriate)
        )

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { commentToReport = null },
            title = { Text(stringResource(id = R.string.report_thought_dialog_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(id = R.string.report_thought_dialog_msg),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    reasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onReportComment(comment.id, selectedReason)
                        commentToReport = null
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.report_action),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { commentToReport = null }) {
                    Text(stringResource(id = R.string.cancel_action))
                }
            }
        )
    }

    if (isPostSheetVisible) {
        PostCommentBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    isPostSheetVisible = false
                }
            },
            onPostComment = { content, isSpoiler ->
                onPostComment(content, isSpoiler)
                coroutineScope.launch {
                    sheetState.hide()
                    isPostSheetVisible = false
                }
            }
        )
    }
}
