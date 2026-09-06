package com.ssverma.shared.ui.component.community

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.R

@Composable
fun DiscussionActionsRow(
    isUpvotedByMe: Boolean,
    displayUpvotesCount: String,
    onUpvoteClick: () -> Unit,
    onReplyClick: (() -> Unit)?,
    repliesCount: Int,
    isRepliesExpanded: Boolean,
    onToggleRepliesExpand: (() -> Unit)?,
    isNestedReply: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isHeartPopped by remember { mutableStateOf(false) }
    var lastUpvoteClickTime by remember { mutableStateOf(0L) }

    val heartScale by animateFloatAsState(
        targetValue = if (isHeartPopped) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { isHeartPopped = false },
        label = "HeartScaleAnimation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            // Reply action
            if (onReplyClick != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onReplyClick()
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Reply,
                        contentDescription = stringResource(id = R.string.reply_action),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier.size(size = 16.dp)
                    )
                    Spacer(modifier = Modifier.width(width = 4.dp))
                    Text(
                        text = stringResource(id = R.string.reply_action),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }

            // Upvote / Like Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .clickable {
                        val now = System.currentTimeMillis()
                        if (now - lastUpvoteClickTime >= 350L) {
                            lastUpvoteClickTime = now
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isHeartPopped = true
                            onUpvoteClick()
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (isUpvotedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.upvote_cd),
                    tint = if (isUpvotedByMe) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.75f
                    ),
                    modifier = Modifier
                        .size(size = 16.dp)
                        .scale(scale = heartScale)
                )

                if (displayUpvotesCount.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(width = 4.dp))
                    Text(
                        text = displayUpvotesCount,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isUpvotedByMe) FontWeight.Bold else FontWeight.Medium,
                        color = if (isUpvotedByMe) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.75f
                        )
                    )
                }
            }
        }

        // View Replies Pill for root comments (collapsing is handled by the bottom button)
        if (!isNestedReply && repliesCount > 0 && !isRepliesExpanded && onToggleRepliesExpand != null) {
            Surface(
                shape = RoundedCornerShape(size = 12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                onClick = onToggleRepliesExpand
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size = 13.dp)
                    )
                    Spacer(modifier = Modifier.width(width = 5.dp))
                    Text(
                        text = if (isRepliesExpanded) {
                            stringResource(id = R.string.hide_replies)
                        } else {
                            if (repliesCount == 1) {
                                stringResource(id = R.string.view_reply_singular)
                            } else {
                                stringResource(id = R.string.view_replies_count, repliesCount)
                            }
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
