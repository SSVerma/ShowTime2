package com.ssverma.shared.ui.component.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.shared.domain.model.community.EditCommentArgs
import com.ssverma.shared.domain.model.community.PostCommentArgs
import com.ssverma.shared.domain.model.community.ReportCommentArgs
import com.ssverma.shared.domain.model.community.ThreadFilter
import com.ssverma.shared.domain.utils.ContentSafetyFilter
import com.ssverma.shared.domain.utils.ContentSafetyResult
import com.ssverma.shared.ui.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionsScreenContent(
    comments: List<CommentUiModel>,
    selectedFilter: ThreadFilter,
    onFilterSelected: (ThreadFilter) -> Unit,
    mediaTitle: String?,
    onBackPressed: () -> Unit,
    onPostComment: (PostCommentArgs) -> Unit,
    onEditComment: (EditCommentArgs) -> Unit = {},
    onReportComment: (ReportCommentArgs) -> Unit = {},
    onToggleUpvote: (commentId: String) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    modifier: Modifier = Modifier,
    posterImageUrl: String? = null
) {
    var inputContent by remember { mutableStateOf("") }
    var isSpoiler by remember { mutableStateOf(false) }
    var replyingToComment by remember { mutableStateOf<CommentUiModel?>(null) }
    var editingComment by remember { mutableStateOf<CommentUiModel?>(null) }
    var commentToDelete by remember { mutableStateOf<CommentUiModel?>(null) }
    var commentToReport by remember { mutableStateOf<CommentUiModel?>(null) }
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
    val reportSuccessMessage = stringResource(id = R.string.report_submitted_and_hidden_msg)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(connection = scrollBehavior.nestedScrollConnection),
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
            DiscussionInputBar(
                inputContent = inputContent,
                onInputChange = { inputContent = it },
                isSpoiler = isSpoiler,
                onToggleSpoiler = {
                    val newState = !isSpoiler
                    isSpoiler = newState
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = if (newState) spoilerEnabledMessage else spoilerDisabledMessage,
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                replyingToComment = replyingToComment,
                onClearReply = {
                    replyingToComment = null
                    keyboardController?.hide()
                },
                editingComment = editingComment,
                onClearEdit = {
                    editingComment = null
                    inputContent = ""
                    isSpoiler = false
                    keyboardController?.hide()
                },
                onSendClick = {
                    val contentToSend = inputContent.trim()
                    val safetyCheck = ContentSafetyFilter.validateContent(text = contentToSend)
                    if (safetyCheck is ContentSafetyResult.Blocked) {
                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                        coroutineScope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = safetyCheck.reason,
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@DiscussionInputBar
                    }

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
                        if (effectiveParentId != null) {
                            // Replying to thread: auto-expand thread so newly appended reply is visible
                            collapsedCommentIds.remove(effectiveParentId)
                            // Preserve current scroll position (do NOT reset to index 0)
                        } else {
                            // Root comment posted: smooth scroll to top so author immediately sees their new thought
                            coroutineScope.launch {
                                listState.animateScrollToItem(index = 0)
                            }
                        }
                        replyingToComment = null
                    }
                    inputContent = ""
                    isSpoiler = false
                    keyboardController?.hide()
                },
                focusRequester = focusRequester
            )
        }
    ) { innerPadding ->
        val density = LocalDensity.current
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val topBarOffsetDp = with(density) { scrollBehavior.state.heightOffset.toDp() }
        val contentTopPadding = statusBarTop + 64.dp + topBarOffsetDp

        val isScrolled = remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .padding(top = contentTopPadding)
        ) {
            // Cinematic Media Context Header Card
            if (!mediaTitle.isNullOrBlank() || !posterImageUrl.isNullOrBlank()) {
                item(contentType = "media_context_header") {
                    Surface(
                        shape = RoundedCornerShape(size = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(all = 12.dp)
                        ) {
                            if (!posterImageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(posterImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = mediaTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 44.dp, height = 62.dp)
                                        .clip(shape = RoundedCornerShape(size = 8.dp))
                                )
                                Spacer(modifier = Modifier.width(width = 12.dp))
                            }

                            Column(modifier = Modifier.weight(weight = 1f)) {
                                if (!mediaTitle.isNullOrBlank()) {
                                    Text(
                                        text = mediaTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(height = 2.dp))
                                }
                                Text(
                                    text = stringResource(
                                        id = R.string.media_context_thoughts_count,
                                        comments.size
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(id = R.string.media_context_tagline),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }

            // Sticky M3 Expressive Filter Chips
            stickyHeader {
                DiscussionFilterBar(
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected,
                    isScrolled = isScrolled.value
                )
            }

            // Feed Items
            if (comments.isEmpty()) {
                item(contentType = "empty_state") {
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
                                modifier = Modifier.size(size = 48.dp)
                            )
                            Spacer(modifier = Modifier.height(height = 12.dp))
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
                    items = comments,
                    key = { it.id },
                    contentType = { "discussion_thread" }
                ) { commentUi ->
                    val isRepliesExpanded = commentUi.id !in collapsedCommentIds

                    DiscussionThreadItem(
                        comment = commentUi,
                        onUpvoteClick = onToggleUpvote,
                        onReplyClick = { target ->
                            replyingToComment = target
                            editingComment = null
                            coroutineScope.launch {
                                delay(100)
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        },
                        onEditClick = { target ->
                            editingComment = target
                            replyingToComment = null
                            inputContent = target.content
                            isSpoiler = target.isSpoiler
                            coroutineScope.launch {
                                delay(100)
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        },
                        onDeleteClick = { target ->
                            commentToDelete = target
                        },
                        onReportClick = { target ->
                            commentToReport = target
                        },
                        isRepliesExpanded = isRepliesExpanded,
                        onToggleRepliesExpand = {
                            val isExpanding = commentUi.id in collapsedCommentIds
                            if (isExpanding) {
                                collapsedCommentIds.remove(commentUi.id)
                            } else {
                                collapsedCommentIds.add(commentUi.id)
                            }
                            val itemIndex = comments.indexOfFirst { it.id == commentUi.id }
                            if (itemIndex != -1) {
                                val hasMediaHeader =
                                    !mediaTitle.isNullOrBlank() || !posterImageUrl.isNullOrBlank()
                                val targetIndex = (if (hasMediaHeader) 1 else 0) + 1 + itemIndex
                                coroutineScope.launch {
                                    listState.animateScrollToItem(
                                        index = targetIndex,
                                        scrollOffset = 0
                                    )
                                }
                            }
                        }
                    )
                }

                item(contentType = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(height = 16.dp))
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
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = reportSuccessMessage,
                        duration = SnackbarDuration.Short
                    )
                }
            },
            onDismiss = { commentToReport = null }
        )
    }
}
