package com.ssverma.common.ui.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.shared.ui.R

@Composable
fun DiscussionCommentRow(
    comment: CommentUiModel,
    onUpvoteClick: () -> Unit,
    onReplyClick: (() -> Unit)?,
    onEditClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?,
    onReportClick: (() -> Unit)?,
    isRepliesExpanded: Boolean,
    onToggleRepliesExpand: (() -> Unit)?,
    isNestedReply: Boolean,
    modifier: Modifier = Modifier,
    showSpine: Boolean = false
) {
    var isSpoilerRevealed by remember(comment.id) { mutableStateOf(!comment.isSpoiler) }
    var isFlaggedRevealed by remember(comment.id) { mutableStateOf(!comment.isFlagged) }
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Min)
    ) {
        // Left Avatar (with optional continuous spine down to nested replies)
        DiscussionAvatarWithSpine(
            authorId = comment.authorId,
            authorName = comment.authorName,
            avatarInitial = comment.avatarInitial,
            avatarUrl = comment.authorAvatarUrl,
            showSpine = showSpine,
            isOwner = comment.isOwner,
            size = if (isNestedReply) 30.dp else 38.dp
        )

        Spacer(modifier = Modifier.width(width = if (isNestedReply) 10.dp else 12.dp))

        // Right Content Column
        Column(
            modifier = Modifier.weight(weight = 1f)
        ) {
            // Header Row: Author, Badges, Time, Options Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(weight = 1f, fill = false)
                ) {
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
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(width = 6.dp))
                    Text(
                        text = comment.displayRelativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 1
                    )

                    if (comment.isEdited) {
                        Spacer(modifier = Modifier.width(width = 4.dp))
                        Text(
                            text = stringResource(id = R.string.edited_tag),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            maxLines = 1
                        )
                    }
                }

                // Options Overflow Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(size = 28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(id = R.string.more_options_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.size(size = 16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(size = 16.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        shadowElevation = 6.dp,
                        tonalElevation = 2.dp,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        if (comment.isOwner) {
                            if (onEditClick != null) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.edit_action),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(size = 18.dp)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onEditClick()
                                    }
                                )
                            }
                            if (onDeleteClick != null) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.delete_action),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(size = 18.dp)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onDeleteClick()
                                    }
                                )
                            }
                        } else if (onReportClick != null) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(id = R.string.report_action),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Flag,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(size = 18.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onReportClick()
                                }
                            )
                        }
                    }
                }
            }

            // Replying to mention pill
            if (comment.replyToAuthorName != null) {
                Text(
                    text = stringResource(id = R.string.replying_to, comment.replyToAuthorName),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(height = 2.dp))

            // Body content
            if (comment.isFlagged && !isFlaggedRevealed) {
                FlaggedCommentShield(
                    onRevealClick = { isFlaggedRevealed = true }
                )
            } else if (comment.isSpoiler) {
                DiscussionSpoilerShield(
                    content = comment.content,
                    isSpoilerRevealed = isSpoilerRevealed,
                    onToggleSpoiler = { isSpoilerRevealed = !isSpoilerRevealed }
                )
            } else {
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(height = 8.dp))

            // Actions row (Like, Reply, View Replies)
            DiscussionActionsRow(
                isUpvotedByMe = comment.isUpvotedByMe,
                displayUpvotesCount = comment.displayUpvotesCount,
                onUpvoteClick = onUpvoteClick,
                onReplyClick = onReplyClick,
                repliesCount = comment.repliesCount,
                isRepliesExpanded = isRepliesExpanded,
                onToggleRepliesExpand = onToggleRepliesExpand,
                isNestedReply = isNestedReply
            )
        }
    }
}
