package com.ssverma.shared.ui.component.community

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.ui.R
import java.util.concurrent.TimeUnit

@Composable
private fun getM3AvatarColors(key: String): Pair<Color, Color> {
    val themeColorPairs = listOf(
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    )
    val index = kotlin.math.abs(key.hashCode()) % themeColorPairs.size
    return themeColorPairs[index]
}

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
    val haptic = LocalHapticFeedback.current
    var isSpoilerRevealed by remember(comment.id) { mutableStateOf(!comment.isSpoiler) }
    var menuExpanded by remember { mutableStateOf(false) }
    val (avatarContainerColor, avatarContentColor) = getM3AvatarColors(comment.authorId)

    OutlinedCard(
        shape = RoundedCornerShape(if (isNestedReply) 12.dp else 16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isNestedReply) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isNestedReply) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.outlinedCardElevation(
            defaultElevation = 0.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isNestedReply) 12.dp else 16.dp)
        ) {
            // Author header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Flat M3 Monogram Avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isNestedReply) 28.dp else 36.dp)
                        .clip(CircleShape)
                        .background(avatarContainerColor)
                ) {
                    Text(
                        text = comment.authorName.take(1).uppercase(),
                        style = if (isNestedReply) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = avatarContentColor
                    )
                }

                Spacer(modifier = Modifier.width(if (isNestedReply) 8.dp else 12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = comment.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${formatRelativeTime(comment.createdAtEpochMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1
                        )
                        if (comment.isEdited) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(id = R.string.edited_tag),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                maxLines = 1
                            )
                        }
                    }

                    if (comment.replyToAuthorName != null) {
                        Text(
                            text = "↳ replying to @${comment.replyToAuthorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }

                // Options Menu (Edit / Delete / Report) with Aesthetic M3 Rounded Styling
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp,
                        tonalElevation = 3.dp,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 14.dp,
                                        vertical = 6.dp
                                    ),
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
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 14.dp,
                                        vertical = 6.dp
                                    ),
                                    onClick = {
                                        menuExpanded = false
                                        onDeleteClick()
                                    }
                                )
                            }
                        } else {
                            if (onReportClick != null) {
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
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 14.dp,
                                        vertical = 6.dp
                                    ),
                                    onClick = {
                                        menuExpanded = false
                                        onReportClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (comment.isSpoiler) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                ) {
                    Text(
                        text = stringResource(id = R.string.spoiler_warning_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment text with Spoiler Shield
            if (comment.isSpoiler && !isSpoilerRevealed) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSpoilerRevealed = true
                        }
                        .padding(14.dp)
                ) {
                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        modifier = Modifier.blur(14.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            text = stringResource(id = R.string.tap_to_reveal_spoiler),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )

                if (comment.isSpoiler) {
                    Text(
                        text = stringResource(id = R.string.tap_to_hide_spoiler),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clickable {
                                isSpoilerRevealed = false
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Flat M3 Interactive Action Pills (Replies Toggle + Reply + Upvote)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isNestedReply && (comment.replies.isNotEmpty() || comment.repliesCount > 0)) {
                        // Replies count / expand toggle pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            onClick = { onToggleRepliesExpand?.invoke() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isRepliesExpanded && comment.replies.isNotEmpty()) {
                                        stringResource(id = R.string.hide_replies)
                                    } else {
                                        if (comment.repliesCount == 1) {
                                            stringResource(id = R.string.reply_count_singular)
                                        } else {
                                            stringResource(
                                                id = R.string.replies_count,
                                                comment.repliesCount
                                            )
                                        }
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (onReplyClick != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                onClick = { onReplyClick() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Reply,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = stringResource(id = R.string.reply_action),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else if (onReplyClick != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            onClick = { onReplyClick() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Reply,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = stringResource(id = R.string.reply_action),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Upvote Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (comment.isUpvotedByMe) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onUpvoteClick()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = if (comment.isUpvotedByMe) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = stringResource(id = R.string.upvote_cd),
                            tint = if (comment.isUpvotedByMe) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(15.dp)
                        )

                        if (comment.upvotesCount > 0) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${comment.upvotesCount}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (comment.isUpvotedByMe) FontWeight.Bold else FontWeight.Medium,
                                color = if (comment.isUpvotedByMe) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatRelativeTime(epochMs: Long): String {
    val diff = System.currentTimeMillis() - epochMs
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        else -> "${TimeUnit.MILLISECONDS.toDays(diff) / 7}w ago"
    }
}
