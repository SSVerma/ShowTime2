package com.ssverma.shared.ui.component.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.EditCommentArgs
import com.ssverma.shared.domain.model.community.PostCommentArgs
import com.ssverma.shared.domain.utils.ContentSafetyFilter
import com.ssverma.shared.domain.utils.ContentSafetyResult
import com.ssverma.shared.ui.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MaxCommentCharLimit = 500

enum class ThreadFilter {
    ALL,
    SPOILER_FREE,
    TOP_UPVOTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionsScreenContent(
    discussions: List<Comment>,
    mediaTitle: String?,
    onBackPressed: () -> Unit,
    onPostComment: (PostCommentArgs) -> Unit,
    onEditComment: (EditCommentArgs) -> Unit = {},
    onReportComment: (commentId: String, reason: String) -> Unit = { _, _ -> },
    onToggleUpvote: (commentId: String) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
    var inputContent by remember { mutableStateOf("") }
    var isSpoiler by remember { mutableStateOf(false) }
    var replyingToComment by remember { mutableStateOf<Comment?>(null) }
    var editingComment by remember { mutableStateOf<Comment?>(null) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    var commentToReport by remember { mutableStateOf<Comment?>(null) }
    var selectedFilter by remember { mutableStateOf(ThreadFilter.ALL) }
    val collapsedCommentIds = remember { mutableStateListOf<String>() }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val spoilerEnabledMessage = stringResource(id = R.string.spoiler_mode_enabled_msg)
    val spoilerDisabledMessage = stringResource(id = R.string.spoiler_mode_disabled_msg)

    val filteredComments = remember(discussions, selectedFilter) {
        val baseList = when (selectedFilter) {
            ThreadFilter.ALL -> discussions.sortedByDescending { it.createdAtEpochMs }
            ThreadFilter.SPOILER_FREE -> discussions.filter { !it.isSpoiler }
                .sortedByDescending { it.createdAtEpochMs }

            ThreadFilter.TOP_UPVOTED -> discussions.sortedWith(
                compareByDescending<Comment> { it.upvotesCount }.thenByDescending { it.createdAtEpochMs }
            )
        }

        baseList.map { root ->
            val sortedReplies = if (selectedFilter == ThreadFilter.TOP_UPVOTED) {
                root.replies.sortedWith(
                    compareByDescending<Comment> { it.upvotesCount }.thenByDescending { it.createdAtEpochMs }
                )
            } else {
                root.replies.sortedBy { it.createdAtEpochMs }
            }
            root.copy(replies = sortedReplies)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ShowTimeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.discussions),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (!mediaTitle.isNullOrBlank()) {
                            Text(
                                text = mediaTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                onBackPressed = onBackPressed,
                scrollBehavior = scrollBehavior,
                showBottomShadow = false,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    androidx.compose.material3.HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
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
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Send,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
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
                                            replyingToComment = null
                                            keyboardController?.hide()
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(id = R.string.clear_reply_cd),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(14.dp)
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
                        editingComment?.let { editTarget ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${stringResource(id = R.string.editing_thought_hint)}: \"${
                                            editTarget.content.take(
                                                28
                                            )
                                        }...\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            editingComment = null
                                            inputContent = ""
                                            isSpoiler = false
                                            keyboardController?.hide()
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(id = R.string.clear_reply_cd),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Input & Send Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Icon-only Spoiler Toggle Button
                        FilledTonalIconButton(
                            onClick = {
                                val newState = !isSpoiler
                                isSpoiler = newState
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(
                                        message = if (newState) spoilerEnabledMessage else spoilerDisabledMessage,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isSpoiler) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                },
                                contentColor = if (isSpoiler) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = stringResource(id = R.string.spoiler_shield_cd),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Aesthetic Bordered Text Field
                        OutlinedTextField(
                            value = inputContent,
                            onValueChange = {
                                if (it.length <= MaxCommentCharLimit) {
                                    inputContent = it
                                }
                            },
                            placeholder = {
                                Text(
                                    text = when {
                                        editingComment != null -> stringResource(id = R.string.editing_thought_hint)
                                        replyingToComment != null -> stringResource(
                                            id = R.string.replying_to,
                                            replyingToComment?.authorName.orEmpty()
                                        )

                                        else -> stringResource(id = R.string.post_thought_hint)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = if (inputContent.isNotEmpty()) {
                                {
                                    val remaining = MaxCommentCharLimit - inputContent.length
                                    Text(
                                        text = "$remaining",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (remaining < 50) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        },
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                }
                            } else null,
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.25f
                                ),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.12f
                                ),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(
                                    alpha = 0.7f
                                )
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send / Save Button
                        val canSend = inputContent.isNotBlank()
                        FilledIconButton(
                            onClick = {
                                if (canSend) {
                                    val contentToSend = inputContent.trim()
                                    val safetyCheck =
                                        ContentSafetyFilter.validateContent(contentToSend)
                                    if (safetyCheck is ContentSafetyResult.Blocked) {
                                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                        coroutineScope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            snackbarHostState.showSnackbar(
                                                message = safetyCheck.reason,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        return@FilledIconButton
                                    }

                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val editTarget = editingComment
                                    if (editTarget != null) {
                                        onEditComment(
                                            EditCommentArgs(
                                                commentId = editTarget.id,
                                                newContent = contentToSend,
                                                isSpoiler = isSpoiler
                                            )
                                        )
                                        editingComment = null
                                    } else {
                                        val parent = replyingToComment
                                        val effectiveParentId =
                                            if (parent?.parentId != null) parent.parentId else parent?.id
                                        val effectiveReplyToAuthorName = parent?.authorName
                                        onPostComment(
                                            PostCommentArgs(
                                                content = contentToSend,
                                                isSpoiler = isSpoiler,
                                                parentId = effectiveParentId,
                                                replyToAuthor = effectiveReplyToAuthorName
                                            )
                                        )
                                        replyingToComment = null
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    }
                                    inputContent = ""
                                    isSpoiler = false
                                    keyboardController?.hide()
                                }
                            },
                            enabled = canSend,
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.35f
                                ),
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.4f
                                )
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = if (editingComment != null) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.Send,
                                contentDescription = stringResource(id = if (editingComment != null) R.string.save_action else R.string.send_action_cd),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val density = LocalDensity.current
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val topBarOffsetDp = with(density) { scrollBehavior.state.heightOffset.toDp() }
        val contentTopPadding = statusBarTop + 64.dp + topBarOffsetDp

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .padding(top = contentTopPadding)
        ) {
            // Sticky Filter Chips Header with bottom elevation
            stickyHeader {
                val isScrolled = remember {
                    androidx.compose.runtime.derivedStateOf {
                        listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
                    }
                }
                val elevation by androidx.compose.animation.core.animateDpAsState(
                    targetValue = if (isScrolled.value) 4.dp else 0.dp,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
                    label = "FilterChipsElevation"
                )

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = elevation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == ThreadFilter.ALL,
                            onClick = { selectedFilter = ThreadFilter.ALL },
                            label = { Text(text = stringResource(id = R.string.filter_all)) }
                        )

                        FilterChip(
                            selected = selectedFilter == ThreadFilter.SPOILER_FREE,
                            onClick = { selectedFilter = ThreadFilter.SPOILER_FREE },
                            label = { Text(text = stringResource(id = R.string.filter_spoiler_free)) }
                        )

                        FilterChip(
                            selected = selectedFilter == ThreadFilter.TOP_UPVOTED,
                            onClick = { selectedFilter = ThreadFilter.TOP_UPVOTED },
                            label = { Text(text = stringResource(id = R.string.filter_top_upvoted)) }
                        )
                    }
                }
            }

            // Feed
            if (filteredComments.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp, start = 32.dp, end = 32.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(id = R.string.no_discussions_yet),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredComments,
                    key = { it.id }
                ) { comment ->
                    val isMyComment =
                        comment.isOwner || (currentUserId != null && comment.authorId == currentUserId)
                    val isRepliesExpanded = comment.id !in collapsedCommentIds

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        // Root Comment Card
                        CommentCard(
                            comment = comment,
                            onUpvoteClick = { onToggleUpvote(comment.id) },
                            onEditClick = if (isMyComment) {
                                {
                                    editingComment = comment
                                    replyingToComment = null
                                    inputContent = comment.content
                                    isSpoiler = comment.isSpoiler
                                    coroutineScope.launch {
                                        delay(100)
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    }
                                }
                            } else null,
                            onDeleteClick = if (isMyComment) {
                                { commentToDelete = comment }
                            } else null,
                            onReportClick = if (!isMyComment) {
                                { commentToReport = comment }
                            } else null,
                            onReplyClick = {
                                replyingToComment = comment
                                editingComment = null
                                coroutineScope.launch {
                                    delay(100)
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                }
                            },
                            isRepliesExpanded = isRepliesExpanded,
                            onToggleRepliesExpand = {
                                if (comment.id in collapsedCommentIds) {
                                    collapsedCommentIds.remove(comment.id)
                                } else {
                                    collapsedCommentIds.add(comment.id)
                                }
                            }
                        )

                        // Nested replies if any
                        AnimatedVisibility(
                            visible = isRepliesExpanded && comment.replies.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp)
                            ) {
                                comment.replies.forEach { reply ->
                                    val isMyReply =
                                        reply.isOwner || (currentUserId != null && reply.authorId == currentUserId)
                                    CommentCard(
                                        comment = reply,
                                        onUpvoteClick = { onToggleUpvote(reply.id) },
                                        onEditClick = if (isMyReply) {
                                            {
                                                editingComment = reply
                                                replyingToComment = null
                                                inputContent = reply.content
                                                isSpoiler = reply.isSpoiler
                                                coroutineScope.launch {
                                                    delay(100)
                                                    focusRequester.requestFocus()
                                                    keyboardController?.show()
                                                }
                                            }
                                        } else null,
                                        onDeleteClick = if (isMyReply) {
                                            { commentToDelete = reply }
                                        } else null,
                                        onReportClick = if (!isMyReply) {
                                            { commentToReport = reply }
                                        } else null,
                                        onReplyClick = {
                                            replyingToComment = reply
                                            editingComment = null
                                            coroutineScope.launch {
                                                delay(100)
                                                focusRequester.requestFocus()
                                                keyboardController?.show()
                                            }
                                        },
                                        isNestedReply = true
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    commentToDelete?.let { comment ->
        AlertDialog(
            onDismissRequest = { commentToDelete = null },
            title = { Text(text = stringResource(id = R.string.delete_thought_dialog_title)) },
            text = { Text(text = stringResource(id = R.string.delete_thought_dialog_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteComment(comment.id)
                        commentToDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_action),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { commentToDelete = null }) {
                    Text(text = stringResource(id = R.string.cancel_action))
                }
            }
        )
    }

    // Report Dialog
    commentToReport?.let { comment ->
        val reportReasons = listOf(
            stringResource(id = R.string.report_reason_spam),
            stringResource(id = R.string.report_reason_harassment),
            stringResource(id = R.string.report_reason_spoiler),
            stringResource(id = R.string.report_reason_inappropriate)
        )
        var selectedReason by remember { mutableStateOf(reportReasons.first()) }
        val reportSuccessMessage = stringResource(id = R.string.report_submitted_msg)

        AlertDialog(
            onDismissRequest = { commentToReport = null },
            title = { Text(text = stringResource(id = R.string.report_thought_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.report_thought_dialog_msg),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    reportReasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
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
                TextButton(
                    onClick = {
                        onReportComment(comment.id, selectedReason)
                        commentToReport = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = reportSuccessMessage,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                ) {
                    Text(text = stringResource(id = R.string.report_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { commentToReport = null }) {
                    Text(text = stringResource(id = R.string.cancel_action))
                }
            }
        )
    }
}
