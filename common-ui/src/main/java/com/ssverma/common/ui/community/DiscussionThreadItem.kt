package com.ssverma.common.ui.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.R

/**
 * YouTube and Twitter-style curved branch connector (└─)
 * Curves 90 degrees right directly into the reply's avatar.
 */
@Composable
fun ThreadSpineConnector(
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val curveRadius = 12.dp.toPx()
        val startX = 19.dp.toPx() // Centers with 38.dp root avatar column
        val avatarCenterY = 15.dp.toPx() // Centers with 30.dp nested avatar
        val endX = size.width

        // Vertical line down from top to the curve (seamless continuation from root comment spine!)
        drawLine(
            color = lineColor,
            start = Offset(startX, 0f),
            end = Offset(startX, avatarCenterY - curveRadius),
            strokeWidth = strokeWidth
        )

        // Arc turning 90 degrees right directly into the reply avatar
        val path = Path().apply {
            moveTo(startX, avatarCenterY - curveRadius)
            quadraticTo(
                startX, avatarCenterY,
                startX + curveRadius, avatarCenterY
            )
            lineTo(endX, avatarCenterY)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth)
        )

        // If not the last reply in this thread, continue vertical line down to the next reply
        if (!isLast) {
            drawLine(
                color = lineColor,
                start = Offset(startX, avatarCenterY - curveRadius),
                end = Offset(startX, size.height),
                strokeWidth = strokeWidth
            )
        }
    }
}

@Composable
fun DiscussionThreadItem(
    comment: CommentUiModel,
    onUpvoteClick: (commentId: String) -> Unit,
    onReplyClick: (CommentUiModel) -> Unit,
    onEditClick: (CommentUiModel) -> Unit,
    onDeleteClick: (CommentUiModel) -> Unit,
    onReportClick: (CommentUiModel) -> Unit,
    isRepliesExpanded: Boolean,
    onToggleRepliesExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasReplies = comment.replies.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(height = 12.dp))

        // Root Comment (with continuous spine connecting to replies when expanded)
        DiscussionCommentRow(
            comment = comment,
            onUpvoteClick = { onUpvoteClick(comment.id) },
            onReplyClick = { onReplyClick(comment) },
            onEditClick = if (comment.isOwner) {
                { onEditClick(comment) }
            } else null,
            onDeleteClick = if (comment.isOwner) {
                { onDeleteClick(comment) }
            } else null,
            onReportClick = if (!comment.isOwner) {
                { onReportClick(comment) }
            } else null,
            isRepliesExpanded = isRepliesExpanded,
            onToggleRepliesExpand = onToggleRepliesExpand,
            isNestedReply = false,
            showSpine = isRepliesExpanded && hasReplies
        )

        // Nested Replies with YouTube/Twitter-Style Branching
        AnimatedVisibility(
            visible = isRepliesExpanded && hasReplies,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                val lastIndex = comment.replies.lastIndex
                comment.replies.forEachIndexed { index, reply ->
                    val isLast = index == lastIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(intrinsicSize = IntrinsicSize.Min)
                            .padding(bottom = 8.dp)
                    ) {
                        // Curved branch connector
                        ThreadSpineConnector(
                            isLast = isLast,
                            modifier = Modifier
                                .width(width = 38.dp)
                                .fillMaxHeight()
                        )

                        // Reply row
                        DiscussionCommentRow(
                            comment = reply,
                            onUpvoteClick = { onUpvoteClick(reply.id) },
                            onReplyClick = { onReplyClick(reply) },
                            onEditClick = if (reply.isOwner) {
                                { onEditClick(reply) }
                            } else null,
                            onDeleteClick = if (reply.isOwner) {
                                { onDeleteClick(reply) }
                            } else null,
                            onReportClick = if (!reply.isOwner) {
                                { onReportClick(reply) }
                            } else null,
                            isRepliesExpanded = false,
                            onToggleRepliesExpand = null,
                            isNestedReply = true,
                            modifier = Modifier.weight(weight = 1f)
                        )
                    }
                }

                // YouTube-style "Hide replies" pill at bottom of thread
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 38.dp, top = 2.dp, bottom = 6.dp)
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .clickable(onClick = onToggleRepliesExpand)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size = 14.dp)
                    )
                    Spacer(modifier = Modifier.width(width = 4.dp))
                    Text(
                        text = stringResource(id = R.string.hide_replies),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(height = 14.dp))

        // Hairline Divider separating discussion threads
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    }
}
