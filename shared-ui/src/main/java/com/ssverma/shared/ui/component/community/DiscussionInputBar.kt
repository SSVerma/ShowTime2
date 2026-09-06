package com.ssverma.shared.ui.component.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.R

private const val MaxCommentCharLimit = 500

@Composable
fun DiscussionInputBar(
    inputContent: String,
    onInputChange: (String) -> Unit,
    isSpoiler: Boolean,
    onToggleSpoiler: () -> Unit,
    replyingToComment: CommentUiModel?,
    onClearReply: () -> Unit,
    editingComment: CommentUiModel?,
    onClearEdit: () -> Unit,
    onSendClick: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    currentUserInitial: String = "Y",
    currentUserAvatarUrl: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val canSend = inputContent.isNotBlank()
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )

            // Replying Indicator Banner
            AnimatedVisibility(
                visible = replyingToComment != null && editingComment == null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                replyingToComment?.let { replyTarget ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(weight = 1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(size = 14.dp)
                                )
                                Spacer(modifier = Modifier.width(width = 6.dp))
                                Text(
                                    text = stringResource(
                                        id = R.string.replying_to,
                                        replyTarget.authorName
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    isExpanded = false
                                    onClearReply()
                                },
                                modifier = Modifier.size(size = 20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(id = R.string.clear_reply_cd),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(size = 14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Editing Indicator Banner
            AnimatedVisibility(
                visible = editingComment != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                editingComment?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.editing_thought_hint),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(weight = 1f)
                            )
                            IconButton(
                                onClick = {
                                    isExpanded = false
                                    onClearEdit()
                                },
                                modifier = Modifier.size(size = 20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(id = R.string.clear_reply_cd),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(size = 14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Composer Row (Left Spoiler Shield + Expandable Pill + Animated Send)
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Left Dedicated Spoiler Shield Button (Replaces cryptic "Y" avatar circle)
                Surface(
                    shape = CircleShape,
                    color = if (isSpoiler) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.4f
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSpoiler) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = 0.3f
                        )
                    ),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleSpoiler()
                    },
                    modifier = Modifier
                        .size(size = 40.dp)
                        .padding(bottom = 2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = stringResource(id = R.string.spoiler_shield_cd),
                            tint = if (isSpoiler) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.7f
                            ),
                            modifier = Modifier.size(size = 20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(width = 8.dp))

                // Pill Container (Gemini-style expandable writing space)
                Surface(
                    shape = RoundedCornerShape(size = 20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .weight(weight = 1f)
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            BasicTextField(
                                value = inputContent,
                                onValueChange = {
                                    if (it.length <= MaxCommentCharLimit) {
                                        onInputChange(it)
                                    }
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
                                minLines = if (isExpanded) 5 else 1,
                                maxLines = if (isExpanded) 12 else 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 14.dp,
                                        end = 36.dp,
                                        top = 10.dp,
                                        bottom = 10.dp
                                    )
                                    .focusRequester(focusRequester = focusRequester),
                                decorationBox = { innerTextField ->
                                    if (inputContent.isEmpty()) {
                                        Text(
                                            text = when {
                                                editingComment != null -> stringResource(id = R.string.editing_thought_hint)
                                                replyingToComment != null -> stringResource(
                                                    id = R.string.replying_to,
                                                    replyingToComment.authorName
                                                )

                                                else -> stringResource(id = R.string.post_thought_hint)
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.55f
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            // Gemini-style Expand / Collapse Icon Button pinned strictly to Top-Right
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isExpanded = !isExpanded
                                },
                                modifier = Modifier
                                    .align(alignment = Alignment.TopEnd)
                                    .padding(
                                        top = 4.dp,
                                        end = 4.dp
                                    )
                                    .size(size = 32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Rounded.CloseFullscreen else Icons.Rounded.OpenInFull,
                                    contentDescription = stringResource(
                                        id = if (isExpanded) R.string.collapse_input_cd else R.string.expand_input_cd
                                    ),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                    modifier = Modifier.size(size = 18.dp)
                                )
                            }
                        }

                        // Character limit counter (only when typing significant text)
                        if (inputContent.length > 250) {
                            val remaining = MaxCommentCharLimit - inputContent.length
                            Text(
                                text = "$remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (remaining < 50) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.6f
                                ),
                                modifier = Modifier
                                    .align(alignment = Alignment.End)
                                    .padding(end = 12.dp, bottom = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(width = 8.dp))

                // Animated Send Button with spring pop scale
                val sendButtonScale by animateFloatAsState(
                    targetValue = if (canSend) 1f else 0.88f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "SendButtonScale"
                )

                FilledIconButton(
                    onClick = {
                        if (canSend) {
                            isExpanded = false
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSendClick()
                        }
                    },
                    enabled = canSend,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .size(size = 40.dp)
                        .scale(scale = sendButtonScale)
                ) {
                    Icon(
                        imageVector = if (editingComment != null) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.Send,
                        contentDescription = stringResource(id = if (editingComment != null) R.string.save_action else R.string.send_action_cd),
                        modifier = Modifier.size(size = 18.dp)
                    )
                }
            }
        }
    }
}
