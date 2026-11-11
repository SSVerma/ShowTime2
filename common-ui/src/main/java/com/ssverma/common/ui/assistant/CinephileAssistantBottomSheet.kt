package com.ssverma.common.ui.assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ssverma.common.ui.R
import com.ssverma.common.ui.a2ui.A2UiComposeRenderer
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.modifier.preventSheetFluctuation
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ai.a2ui.model.A2UiAction
import com.ssverma.shared.ai.domain.model.CinephileChatThread

/**
 * Top-level stateful BottomSheet hosting the Cinephile AI Assistant.
 *
 * Adheres strictly to ShowTime UI standards:
 * - Uses [ShowTimeBottomSheet] with nested scroll fluctuation prevention.
 * - Displays immediate thinking indicators before the on-device model begins emitting tokens.
 * - Hides raw A2UI code fences during streaming, rendering a polished generation badge instead.
 * - Supports multi-turn generic conversation threads with local persistence.
 * - Uses a sleek Material 3 rounded pill input bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinephileAssistantBottomSheet(
    onDismissRequest: () -> Unit,
    onOpenMedia: (tmdbId: Int, mediaType: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CinephileAssistantViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        preventScrollFluctuation = true,
        modifier = modifier,
    ) {
        CinephileAssistantContent(
            uiState = uiState,
            onPromptChange = viewModel::updatePromptInput,
            onSendPrompt = { viewModel.submitPrompt() },
            onAction = { action ->
                viewModel.handleA2UiAction(
                    action = action,
                    onOpenMedia = onOpenMedia,
                )
            },
            onNewChat = viewModel::startNewThread,
            onSelectThread = viewModel::selectThread,
            onDeleteThread = viewModel::deleteThread,
            onToggleHistory = { viewModel.toggleThreadHistory(!uiState.isThreadHistoryOpen) },
        )
    }
}

@Composable
fun CinephileAssistantContent(
    uiState: CinephileAssistantUiState,
    onPromptChange: (String) -> Unit,
    onSendPrompt: () -> Unit,
    onAction: (A2UiAction) -> Unit,
    modifier: Modifier = Modifier,
    onNewChat: () -> Unit = {},
    onSelectThread: (String) -> Unit = {},
    onDeleteThread: (String) -> Unit = {},
    onToggleHistory: () -> Unit = {},
) {
    var threadPendingDelete by remember { mutableStateOf<CinephileChatThread?>(null) }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive, streaming completes, or active thread changes
    LaunchedEffect(uiState.messages.size, uiState.isStreaming, uiState.currentThreadId) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Keep bottom visible during active streaming as tokens/chunks arrive
    val lastMessage = uiState.messages.lastOrNull()
    val streamingLength = (lastMessage as? CinephileMessageItem.StreamingChunk)?.partialText?.length
    val streamingTool = (lastMessage as? CinephileMessageItem.StreamingChunk)?.activeToolName
    LaunchedEffect(streamingLength, streamingTool) {
        if (uiState.messages.isNotEmpty() && streamingLength != null) {
            val isAtBottom = !listState.canScrollForward ||
                    listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == uiState.messages.size - 1
            if (isAtBottom) {
                listState.scrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium),
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.cinephile_ai_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = uiState.currentThreadTitle
                        ?: stringResource(id = R.string.cinephile_ai_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (uiState.threads.isNotEmpty()) {
                IconButton(
                    onClick = onToggleHistory,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "History",
                        tint = if (uiState.isThreadHistoryOpen) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            IconButton(
                onClick = onNewChat,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddComment,
                    contentDescription = "New Chat",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        // Thread History Dropdown Sheet
        AnimatedVisibility(
            visible = uiState.isThreadHistoryOpen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small,
                    ),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Past Conversations",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        TextButton(onClick = onNewChat) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Chat", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                    ) {
                        items(uiState.threads, key = { it.id }) { thread ->
                            val isSelected = thread.id == uiState.currentThreadId
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        } else {
                                            Color.Transparent
                                        },
                                    )
                                    .clickable { onSelectThread(thread.id) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = thread.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = { threadPendingDelete = thread },
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = stringResource(id = R.string.cinephile_ai_delete_confirm),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delete Thread Confirmation Dialog
        threadPendingDelete?.let { thread ->
            AlertDialog(
                onDismissRequest = { threadPendingDelete = null },
                title = {
                    Text(
                        text = stringResource(id = R.string.cinephile_ai_delete_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                text = {
                    Text(
                        text = stringResource(
                            id = R.string.cinephile_ai_delete_dialog_message,
                            thread.title,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteThread(thread.id)
                            threadPendingDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(
                            text = stringResource(id = R.string.cinephile_ai_delete_confirm),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { threadPendingDelete = null }) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Message Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .preventSheetFluctuation(),
            contentPadding = PaddingValues(bottom = MaterialTheme.spacing.small),
            reverseLayout = false,
        ) {
            items(
                items = uiState.messages,
                key = { message ->
                    when (message) {
                        is CinephileMessageItem.UserPrompt -> message.id
                        is CinephileMessageItem.AgentDocument -> message.id
                        is CinephileMessageItem.StreamingChunk -> message.id
                    }
                },
                contentType = { message ->
                    when (message) {
                        is CinephileMessageItem.UserPrompt -> "UserPrompt"
                        is CinephileMessageItem.AgentDocument -> "AgentDocument"
                        is CinephileMessageItem.StreamingChunk -> "StreamingChunk"
                    }
                },
            ) { message ->
                when (message) {
                    is CinephileMessageItem.UserPrompt -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = MaterialTheme.spacing.medium,
                                    vertical = MaterialTheme.spacing.extraSmall,
                                ),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = message.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(MaterialTheme.spacing.medium),
                                )
                            }
                        }
                    }

                    is CinephileMessageItem.AgentDocument -> {
                        A2UiComposeRenderer(
                            document = message.document,
                            onAction = onAction,
                        )
                    }

                    is CinephileMessageItem.StreamingChunk -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = MaterialTheme.spacing.medium,
                                    vertical = MaterialTheme.spacing.small,
                                ),
                        ) {
                            if (message.activeToolName != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small),
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                    Text(
                                        text = stringResource(
                                            id = R.string.cinephile_ai_executing_tool,
                                            message.activeToolName,
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            } else if (message.partialText.isBlank()) {
                                // Immediate thinking indicator before tokens arrive
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.small),
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                    Text(
                                        text = stringResource(id = R.string.cinephile_ai_thinking),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }

                            // Filter out raw JSON / A2UI code fences while streaming
                            val fenceCandidate =
                                listOf("```a2ui", "```json", "```A2UI", "```").firstOrNull {
                                    message.partialText.contains(it)
                                }
                            val displayText = if (fenceCandidate != null) {
                                message.partialText.substring(
                                    0,
                                    message.partialText.indexOf(fenceCandidate)
                                ).trim()
                            } else {
                                message.partialText.trim()
                            }
                            val isGeneratingWidgets = fenceCandidate != null

                            if (displayText.isNotBlank()) {
                                Text(
                                    text = displayText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            if (isGeneratingWidgets) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                    Text(
                                        text = stringResource(id = R.string.cinephile_ai_generating_picks),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Modern Pill Input Bar
        val canSend = uiState.promptInput.isNotBlank() && !uiState.isStreaming
        val sendButtonContainerColor by animateColorAsState(
            targetValue = if (canSend) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            label = "sendButtonColor",
        )
        val sendButtonContentColor by animateColorAsState(
            targetValue = if (canSend) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            },
            label = "sendButtonIconColor",
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.small,
                ),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                ),
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (uiState.promptInput.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.cinephile_ai_prompt_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        BasicTextField(
                            value = uiState.promptInput,
                            onValueChange = onPromptChange,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            IconButton(
                onClick = onSendPrompt,
                enabled = canSend,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = sendButtonContainerColor,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = stringResource(id = R.string.cinephile_ai_send),
                    tint = sendButtonContentColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
