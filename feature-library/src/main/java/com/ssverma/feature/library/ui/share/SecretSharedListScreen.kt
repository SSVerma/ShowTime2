package com.ssverma.feature.library.ui.share

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.domain.utils.ShareMediaUtils
import com.ssverma.shared.ui.component.media.MediaCardFrostedActionButton
import com.ssverma.shared.ui.component.media.MediaCardRatingBadge
import com.ssverma.shared.ui.component.media.ShowTimeMediaGridCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretSharedListScreen(
    shareCode: String,
    onBackClick: () -> Unit,
    onOpenMovieDetails: (Int) -> Unit,
    onOpenTvShowDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SecretSharedListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showRevokeConfirmDialog by remember { mutableStateOf(false) }
    var showAddAllConfirmDialog by remember { mutableStateOf(false) }
    var showCloneConfirmDialog by remember { mutableStateOf(false) }
    var itemToRemove by remember { mutableStateOf<SecretSharedListItem?>(null) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    if (showInfoSheet) {
        SecretShareInfoBottomSheet(
            onDismissRequest = { showInfoSheet = false }
        )
    }

    LaunchedEffect(shareCode) {
        viewModel.init(shareCode)
    }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedbackMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Column {
                    ShowTimeTopAppBar(
                        title = {
                            Text(
                                text = uiState.secretSharedList?.title
                                    ?: stringResource(R.string.secret_share_title),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onBackPressed = onBackClick,
                        scrollBehavior = scrollBehavior,
                        actions = {
                            IconButton(onClick = { showInfoSheet = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = stringResource(R.string.secret_share_info_learn_more),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (uiState.isOwner) {
                                Box {
                                    IconButton(onClick = { showOverflowMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Rounded.MoreVert,
                                            contentDescription = stringResource(R.string.more_options),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showOverflowMenu,
                                        onDismissRequest = { showOverflowMenu = false },
                                        shape = RoundedCornerShape(16.dp),
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.secret_share_revoke_action),
                                                    color = MaterialTheme.colorScheme.error,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Rounded.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                showOverflowMenu = false
                                                showRevokeConfirmDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                    if (uiState.isActionInProgress) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }
            }
        ) { innerPadding ->
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        ShowTimeLoadingIndicator()
                    }
                }

                uiState.isRevoked -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.secret_share_revoked_notice),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(onClick = onBackClick) {
                                Text(stringResource(R.string.close))
                            }
                        }
                    }
                }

                uiState.secretSharedList == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.secret_share_not_found),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(onClick = onBackClick) {
                                Text(stringResource(R.string.close))
                            }
                        }
                    }
                }

                else -> {
                    val list = uiState.secretSharedList!!
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = innerPadding.calculateTopPadding() + 8.dp,
                            bottom = innerPadding.calculateBottomPadding() + 24.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header Section
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Title & Description
                                Text(
                                    text = list.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black
                                )

                                val description = list.description
                                if (!description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Curator & Rating info row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (uiState.isOwner) {
                                                    stringResource(R.string.secret_share_curated_by_you)
                                                } else {
                                                    if (list.ownerName.isBlank() || list.ownerName.equals(
                                                            "Me",
                                                            ignoreCase = true
                                                        )
                                                    ) {
                                                        stringResource(R.string.secret_share_curated_by_friend)
                                                    } else {
                                                        stringResource(
                                                            R.string.secret_share_curated_by,
                                                            list.ownerName
                                                        )
                                                    }
                                                },
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }

                                    if (list.averageRating > 0f) {
                                        Surface(
                                            shape = RoundedCornerShape(100.dp),
                                            color = ListShareColor.RatingGold.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp
                                                ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Star,
                                                    contentDescription = null,
                                                    tint = ListShareColor.RatingGold,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = String.format(
                                                        Locale.US,
                                                        "%.1f",
                                                        list.averageRating
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ListShareColor.RatingGold
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = stringResource(
                                            R.string.items_count,
                                            list.items.size
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Collaborative mode banner
                                if (list.isCollaborative) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.4f
                                            )
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 8.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Group,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.secret_share_collaborator_banner),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action buttons
                                if (list.isCollaborative) {
                                    Button(
                                        onClick = { viewModel.setShowAddDialog(true) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.secret_share_add_title_action),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = { showAddAllConfirmDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (uiState.isAddingToWatchlist) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.BookmarkAdded,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.secret_share_add_all_to_watchlist),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    if (uiState.isOwner) {
                                        FilledTonalButton(
                                            onClick = {
                                                val shareText =
                                                    ShareMediaUtils.buildFormattedSecretListMarkdown(
                                                        title = list.title,
                                                        description = list.description,
                                                        authorName = list.ownerName,
                                                        shareCode = list.shareCode,
                                                        itemTitlesWithRating = list.items.map { it.title to it.voteAvg }
                                                    )
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                    type = "text/plain"
                                                }
                                                val chooser = Intent.createChooser(
                                                    sendIntent,
                                                    context.getString(R.string.secret_share_chooser_title)
                                                )
                                                context.startActivity(chooser)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.secret_share_share_link),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else {
                                        FilledTonalButton(
                                            onClick = { showCloneConfirmDialog = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            if (uiState.isCloning) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Rounded.ContentCopy,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = stringResource(R.string.secret_share_clone_to_lists),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Empty Items notice
                        if (list.items.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.secret_share_empty_items),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Shared Media Items
                        items(list.items, key = { it.mediaId }) { item ->
                            val isAddedByCurrentUser =
                                item.addedByUserId != null && item.addedByUserId == uiState.currentUserId
                            val canRemove =
                                uiState.isOwner || (list.isCollaborative && isAddedByCurrentUser)
                            SharedMediaGridCard(
                                item = item,
                                canRemove = canRemove,
                                isOwner = uiState.isOwner,
                                isAddedByCurrentUser = isAddedByCurrentUser,
                                onRemove = { itemToRemove = item },
                                onClick = {
                                    if (item.mediaType == MediaType.Tv) {
                                        onOpenTvShowDetails(item.mediaId)
                                    } else {
                                        onOpenMovieDetails(item.mediaId)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Add All to Watchlist Confirmation Dialog
            if (showAddAllConfirmDialog) {
                val itemCount = uiState.secretSharedList?.items?.size ?: 0
                AlertDialog(
                    onDismissRequest = { showAddAllConfirmDialog = false },
                    title = { Text(stringResource(R.string.secret_share_add_all_confirm_title)) },
                    text = {
                        Text(stringResource(R.string.secret_share_add_all_confirm_desc, itemCount))
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showAddAllConfirmDialog = false
                                viewModel.addAllToWatchlist()
                            }
                        ) {
                            Text(stringResource(R.string.secret_share_add_all_confirm_action))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddAllConfirmDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            // Clone to My Lists Confirmation Dialog
            if (showCloneConfirmDialog) {
                val listTitle = uiState.secretSharedList?.title.orEmpty()
                val itemCount = uiState.secretSharedList?.items?.size ?: 0
                AlertDialog(
                    onDismissRequest = { showCloneConfirmDialog = false },
                    title = { Text(stringResource(R.string.secret_share_clone_confirm_title)) },
                    text = {
                        Text(
                            stringResource(
                                R.string.secret_share_clone_confirm_desc,
                                listTitle,
                                itemCount
                            )
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showCloneConfirmDialog = false
                                viewModel.cloneToMyLists()
                            }
                        ) {
                            Text(stringResource(R.string.secret_share_clone_confirm_action))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCloneConfirmDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            // Remove Single Item Confirmation Dialog
            itemToRemove?.let { item ->
                AlertDialog(
                    onDismissRequest = { itemToRemove = null },
                    title = { Text(stringResource(R.string.secret_share_remove_item_confirm_title)) },
                    text = {
                        Text(
                            stringResource(
                                R.string.secret_share_remove_item_confirm_desc,
                                item.title
                            )
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val id = item.mediaId
                                itemToRemove = null
                                viewModel.removeMediaFromSharedList(id)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.secret_share_remove_item_confirm_action))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { itemToRemove = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            // Revoke Confirmation Dialog
            if (showRevokeConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showRevokeConfirmDialog = false },
                    title = { Text(stringResource(R.string.secret_share_revoke_action)) },
                    text = { Text(stringResource(R.string.secret_share_revoke_confirm)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                showRevokeConfirmDialog = false
                                viewModel.revokeSecretShare {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.secret_share_revoked_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.secret_share_revoke_confirm_action))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRevokeConfirmDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

        }

        // Full-Screen Search View to Add Titles (Cinema Diary aesthetic)
        AnimatedVisibility(
            visible = uiState.showAddDialog,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 6 })
        ) {
            val existingIds = remember(uiState.secretSharedList) {
                uiState.secretSharedList?.items?.map { it.mediaId }?.toSet().orEmpty()
            }
            SecretShareMediaSearchView(
                searchQuery = uiState.searchQuery,
                selectedFilter = uiState.searchFilter,
                suggestions = uiState.searchResults,
                existingMediaIds = existingIds,
                isSearching = uiState.isSearching,
                onSearchQueryChange = { query, filter ->
                    viewModel.searchMedia(query, filter)
                },
                onClearSearch = { viewModel.clearSearch() },
                onMediaSelected = { viewModel.addMediaToSharedList(it) },
                onDismiss = {
                    viewModel.setShowAddDialog(false)
                    viewModel.clearSearch()
                }
            )
        }
    }
}

@Composable
private fun SharedMediaGridCard(
    item: SecretSharedListItem,
    canRemove: Boolean,
    isOwner: Boolean,
    isAddedByCurrentUser: Boolean = false,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val addedByName = item.addedByName
    val addedByLabel = when {
        isAddedByCurrentUser -> stringResource(R.string.secret_share_added_by_you)
        item.addedByUserId.isNullOrBlank() || addedByName.isNullOrBlank() || addedByName.equals(
            "Me",
            ignoreCase = true
        ) || addedByName == "Creator" -> {
            if (isOwner) null else stringResource(R.string.secret_share_added_by_creator)
        }

        else -> stringResource(R.string.secret_share_added_by, addedByName)
    }

    ShowTimeMediaGridCard(
        title = item.title,
        posterImageUrl = item.posterImageUrl,
        onClick = onClick,
        modifier = modifier,
        topStartBadge = if (item.voteAvg > 0f) {
            { MediaCardRatingBadge(rating = item.voteAvg) }
        } else null,
        topEndAction = if (canRemove) {
            {
                MediaCardFrostedActionButton(
                    icon = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(
                        R.string.secret_share_remove_item_cd,
                        item.title
                    ),
                    onClick = onRemove,
                    tint = MaterialTheme.colorScheme.error,
                    size = 28,
                    hasShadow = true
                )
            }
        } else null,
        subtitle = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item.releaseYear?.let { year ->
                    Text(
                        text = year,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!addedByLabel.isNullOrBlank()) {
                    if (item.releaseYear != null) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    Text(
                        text = addedByLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}
