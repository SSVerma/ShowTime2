package com.ssverma.shared.ui.component.section

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.EditCommentArgs
import com.ssverma.shared.domain.model.community.PostCommentArgs
import com.ssverma.shared.domain.model.community.ReportCommentArgs
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.community.CommentUiModel
import com.ssverma.shared.ui.component.community.DeleteThoughtConfirmationDialog
import com.ssverma.shared.ui.component.community.DiscussionAvatar
import com.ssverma.shared.ui.component.community.PostCommentBottomSheet
import com.ssverma.shared.ui.component.community.ReportThoughtDialog
import com.ssverma.shared.ui.component.community.toUiModel
import kotlinx.coroutines.launch

private const val MaxPreviewDiscussions = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDiscussionsSection(
    discussions: List<Comment>,
    onDiscussionsViewAllClick: () -> Unit,
    onPostComment: (PostCommentArgs) -> Unit,
    onEditComment: (EditCommentArgs) -> Unit = {},
    onReportComment: (ReportCommentArgs) -> Unit = {},
    onToggleUpvote: (commentId: String) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    modifier: Modifier = Modifier,
    currentUserId: String? = null,
    isEnabled: Boolean = true
) {
    if (!isEnabled) return

    val context = LocalContext.current
    var isPostSheetVisible by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<CommentUiModel?>(null) }
    var commentToReport by remember { mutableStateOf<CommentUiModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val previewComments = remember(discussions, currentUserId, context) {
        discussions.take(MaxPreviewDiscussions).map {
            it.toUiModel(context = context, currentUserId = currentUserId)
        }
    }

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.discussions),
                modifier = Modifier.padding(horizontal = 16.dp),
                trailingActionLabel = if (discussions.isNotEmpty()) {
                    stringResource(id = R.string.see_all_with_count, discussions.size)
                } else {
                    stringResource(id = R.string.see_all)
                },
                onTrailingActionClicked = onDiscussionsViewAllClick,
                hideTrailingAction = discussions.isEmpty()
            )
        },
        headerContentSpacing = 10.dp,
        modifier = modifier
    ) {
        // Unified Community Hub Container Card
        Surface(
            shape = RoundedCornerShape(size = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column {
                // 1. Interactive Thought Entry Bar Capsule
                Surface(
                    shape = RoundedCornerShape(size = 20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    ),
                    onClick = {
                        coroutineScope.launch {
                            isPostSheetVisible = true
                            sheetState.show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 10.dp,
                            bottom = if (previewComments.isEmpty()) 10.dp else 8.dp
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddComment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(size = 16.dp)
                        )
                        Spacer(modifier = Modifier.width(width = 8.dp))
                        Text(
                            text = stringResource(id = R.string.post_thought_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(weight = 1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(size = 14.dp)
                        )
                    }
                }

                if (previewComments.isEmpty()) {
                    // Empty state inside unified hub
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(size = 18.dp)
                        )
                        Spacer(modifier = Modifier.width(width = 8.dp))
                        Text(
                            text = stringResource(id = R.string.discussions_section_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.weight(weight = 1f)
                        )
                    }
                } else {
                    // 2. Preview Comments Stream separated by hairline dividers
                    previewComments.forEachIndexed { index, comment ->
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )

                        IntegratedDiscussionRow(
                            comment = comment,
                            onUpvoteClick = { onToggleUpvote(comment.id) },
                            onViewThreadClick = onDiscussionsViewAllClick
                        )
                    }

                    // 3. Integrated Footer Strip when more discussions exist
                    if (discussions.size > MaxPreviewDiscussions) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )

                        Surface(
                            color = Color.Transparent,
                            onClick = onDiscussionsViewAllClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 14.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        id = R.string.view_all_thoughts_in_thread,
                                        discussions.size
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(width = 4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(size = 13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    commentToDelete?.let { comment ->
        DeleteThoughtConfirmationDialog(
            onConfirmDelete = {
                onDeleteComment(comment.id)
                commentToDelete = null
            },
            onDismiss = { commentToDelete = null }
        )
    }

    // Report Dialog
    commentToReport?.let { comment ->
        ReportThoughtDialog(
            onConfirmReport = { reason ->
                onReportComment(ReportCommentArgs(commentId = comment.id, reason = reason))
                commentToReport = null
            },
            onDismiss = { commentToReport = null }
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
            onPostComment = { args ->
                onPostComment(args)
                coroutineScope.launch {
                    sheetState.hide()
                    isPostSheetVisible = false
                }
            }
        )
    }
}

@Composable
private fun IntegratedDiscussionRow(
    comment: CommentUiModel,
    onUpvoteClick: () -> Unit,
    onViewThreadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var lastLikeTime by remember { mutableStateOf(0L) }

    Surface(
        color = Color.Transparent,
        onClick = onViewThreadClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            // Header Row: Avatar, Author, [You] Badge, Time on Left; Intuitive Like Button on Top-Right!
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(weight = 1f, fill = false)
                ) {
                    DiscussionAvatar(
                        authorId = comment.authorId,
                        authorName = comment.authorName,
                        avatarInitial = comment.avatarInitial,
                        avatarUrl = comment.authorAvatarUrl,
                        isOwner = comment.isOwner,
                        size = 24.dp
                    )

                    Spacer(modifier = Modifier.width(width = 8.dp))

                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (comment.isOwner) {
                        Spacer(modifier = Modifier.width(width = 6.dp))
                        Surface(
                            shape = RoundedCornerShape(size = 4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.you_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(width = 6.dp))
                    Text(
                        text = stringResource(id = R.string.bullet_separator),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(width = 6.dp))
                    Text(
                        text = comment.displayRelativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }

                // Top-Right Like Button (Spam-proofed with 350ms debounce)
                Surface(
                    shape = RoundedCornerShape(size = 8.dp),
                    color = if (comment.isUpvotedByMe) MaterialTheme.colorScheme.errorContainer.copy(
                        alpha = 0.35f
                    ) else Color.Transparent,
                    onClick = {
                        val now = System.currentTimeMillis()
                        if (now - lastLikeTime >= 350L) {
                            lastLikeTime = now
                            onUpvoteClick()
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = if (comment.isUpvotedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(id = R.string.upvote_cd),
                            tint = if (comment.isUpvotedByMe) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.65f
                            ),
                            modifier = Modifier.size(size = 15.dp)
                        )
                        if (comment.displayUpvotesCount.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(width = 4.dp))
                            Text(
                                text = comment.displayUpvotesCount,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (comment.isUpvotedByMe) FontWeight.Bold else FontWeight.Medium,
                                color = if (comment.isUpvotedByMe) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.8f
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(height = 4.dp))

            // Body: Truncated text (or spoiler indicator), indented cleanly past the avatar
            if (comment.isFlagged) {
                Surface(
                    shape = RoundedCornerShape(size = 8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(size = 13.dp)
                        )
                        Spacer(modifier = Modifier.width(width = 4.dp))
                        Text(
                            text = stringResource(id = R.string.community_flagged_warning),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (comment.isSpoiler) {
                Surface(
                    shape = RoundedCornerShape(size = 8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(size = 13.dp)
                        )
                        Spacer(modifier = Modifier.width(width = 4.dp))
                        Text(
                            text = stringResource(id = R.string.spoiler_alert_title),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(width = 4.dp))
                        Text(
                            text = "— ${stringResource(id = R.string.tap_to_reveal_spoiler)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }

            // Replies Count Indicator (Only rendered when replies exist!)
            if (comment.repliesCount > 0) {
                Spacer(modifier = Modifier.height(height = 4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size = 12.dp)
                    )
                    Spacer(modifier = Modifier.width(width = 4.dp))
                    Text(
                        text = if (comment.repliesCount == 1) {
                            stringResource(id = R.string.view_reply_singular)
                        } else {
                            stringResource(id = R.string.view_replies_count, comment.repliesCount)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
