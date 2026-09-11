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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.feature.library.R
import com.ssverma.feature.library.ui.share.component.SecretShareRevokeConfirmDialog
import com.ssverma.feature.library.ui.share.component.SecretSharedListAddAllConfirmDialog
import com.ssverma.feature.library.ui.share.component.SecretSharedListCloneConfirmDialog
import com.ssverma.feature.library.ui.share.component.SecretSharedListEmptyItemsState
import com.ssverma.feature.library.ui.share.component.SecretSharedListHeader
import com.ssverma.feature.library.ui.share.component.SecretSharedListNotFoundState
import com.ssverma.feature.library.ui.share.component.SecretSharedListRemoveItemConfirmDialog
import com.ssverma.feature.library.ui.share.component.SecretSharedListRevokedState
import com.ssverma.feature.library.ui.share.component.SecretSharedListTopAppBar
import com.ssverma.feature.library.ui.share.component.SharedMediaGridCard
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.domain.utils.ShareMediaUtils

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
    val gridState = rememberLazyGridState()
    val isScrolledPastHeader by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 40
        }
    }

    var showRevokeConfirmDialog by remember { mutableStateOf(false) }
    var showAddAllConfirmDialog by remember { mutableStateOf(false) }
    var showCloneConfirmDialog by remember { mutableStateOf(false) }
    var itemToRemove by remember { mutableStateOf<SecretSharedListItem?>(null) }
    var showInfoSheet by remember { mutableStateOf(false) }

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
                SecretSharedListTopAppBar(
                    title = uiState.secretSharedList?.title,
                    isTitleVisible = isScrolledPastHeader,
                    isOwner = uiState.isOwner,
                    isActionInProgress = uiState.isActionInProgress,
                    onBackClick = onBackClick,
                    onInfoClick = { showInfoSheet = true },
                    onRevokeClick = { showRevokeConfirmDialog = true },
                    scrollBehavior = scrollBehavior
                )
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
                    SecretSharedListRevokedState(
                        onCloseClick = onBackClick,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                uiState.secretSharedList == null -> {
                    SecretSharedListNotFoundState(
                        onCloseClick = onBackClick,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                else -> {
                    val list = uiState.secretSharedList!!
                    LazyVerticalGrid(
                        state = gridState,
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
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SecretSharedListHeader(
                                list = list,
                                isOwner = uiState.isOwner,
                                isAddingToWatchlist = uiState.isAddingToWatchlist,
                                isCloning = uiState.isCloning,
                                onAddTitleClick = { viewModel.setShowAddDialog(true) },
                                onAddAllToWatchlistClick = { showAddAllConfirmDialog = true },
                                onShareLinkClick = {
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
                                onCloneClick = { showCloneConfirmDialog = true }
                            )
                        }

                        if (list.items.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                SecretSharedListEmptyItemsState()
                            }
                        }

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

            if (showAddAllConfirmDialog) {
                SecretSharedListAddAllConfirmDialog(
                    itemCount = uiState.secretSharedList?.items?.size ?: 0,
                    onConfirm = {
                        showAddAllConfirmDialog = false
                        viewModel.addAllToWatchlist()
                    },
                    onDismissRequest = { showAddAllConfirmDialog = false }
                )
            }

            if (showCloneConfirmDialog) {
                SecretSharedListCloneConfirmDialog(
                    listTitle = uiState.secretSharedList?.title.orEmpty(),
                    itemCount = uiState.secretSharedList?.items?.size ?: 0,
                    onConfirm = {
                        showCloneConfirmDialog = false
                        viewModel.cloneToMyLists()
                    },
                    onDismissRequest = { showCloneConfirmDialog = false }
                )
            }

            itemToRemove?.let { item ->
                SecretSharedListRemoveItemConfirmDialog(
                    itemTitle = item.title,
                    onConfirm = {
                        val id = item.mediaId
                        itemToRemove = null
                        viewModel.removeMediaFromSharedList(id)
                    },
                    onDismissRequest = { itemToRemove = null }
                )
            }

            if (showRevokeConfirmDialog) {
                SecretShareRevokeConfirmDialog(
                    onConfirmRevoke = {
                        showRevokeConfirmDialog = false
                        viewModel.revokeSecretShare {
                            Toast.makeText(
                                context,
                                context.getString(R.string.secret_share_revoked_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onDismissRequest = { showRevokeConfirmDialog = false }
                )
            }
        }

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
                },
                currentUserName = uiState.currentUserName,
                isGoogleUser = uiState.isGoogleUser,
                onUpdateUserName = { viewModel.updateUserDisplayName(it) }
            )
        }
    }
}
