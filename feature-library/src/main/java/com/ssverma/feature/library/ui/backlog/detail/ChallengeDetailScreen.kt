package com.ssverma.feature.library.ui.backlog.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.R
import com.ssverma.feature.library.ui.backlog.component.ChallengeMediaSearchView
import com.ssverma.feature.library.ui.backlog.component.ChallengeShareExportBottomSheet
import com.ssverma.feature.library.ui.backlog.detail.component.CannotRemoveLastTitleDialog
import com.ssverma.feature.library.ui.backlog.detail.component.ChallengeDetailBottomBar
import com.ssverma.feature.library.ui.backlog.detail.component.ChallengeDetailHeroCard
import com.ssverma.feature.library.ui.backlog.detail.component.ChallengeFilterRow
import com.ssverma.feature.library.ui.backlog.detail.component.ChallengeJoinConfirmationDialog
import com.ssverma.feature.library.ui.backlog.detail.component.ChallengeLeaveConfirmationDialog
import com.ssverma.feature.library.ui.backlog.detail.component.ChallengeMediaListItem
import com.ssverma.feature.library.ui.backlog.detail.component.ChallengeProgressGuideCard
import com.ssverma.feature.library.ui.backlog.detail.component.EditGoalDialog
import com.ssverma.feature.library.ui.backlog.detail.component.RemoveMediaItemConfirmDialog
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.ui.component.diary.LogAndRateDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    challengeId: String,
    onBackClick: () -> Unit,
    onOpenMovieDetails: (Int) -> Unit,
    onOpenTvShowDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChallengeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var isShareSheetOpen by remember { mutableStateOf(false) }

    LaunchedEffect(challengeId) {
        viewModel.initChallenge(challengeId)
    }

    Scaffold(
        topBar = {
            ShowTimeTopAppBar(
                title = {
                    Text(
                        text = uiState.progress?.challenge?.title
                            ?: stringResource(R.string.challenges_detail_default_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                onBackPressed = onBackClick,
                actions = {
                    uiState.progress?.let { progress ->
                        if (progress.challenge.isCustom) {
                            IconButton(onClick = viewModel::openEditMetadataDialog) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = stringResource(R.string.challenges_edit_goal_title)
                                )
                            }
                        }
                        if (uiState.isJoined) {
                            IconButton(onClick = { isShareSheetOpen = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = stringResource(R.string.challenges_share_card_btn)
                                )
                            }
                        }
                        if (progress.challenge.isCustom) {
                            IconButton(onClick = viewModel::requestLeaveConfirmation) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = stringResource(R.string.challenges_delete_goal_btn),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            uiState.progress?.let { progress ->
                ChallengeDetailBottomBar(
                    progress = progress,
                    isJoined = uiState.isJoined,
                    onJoinClick = viewModel::requestJoinConfirmation,
                    onLeaveClick = viewModel::requestLeaveConfirmation,
                    onShareClick = { isShareSheetOpen = true }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        if (uiState.isLoading && uiState.progress == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CircularProgressIndicator()
            }
        } else {
            val progress = uiState.progress
            if (progress != null) {
                val watchedIdSet = progress.watchedItems.map { it.id to it.mediaType }.toSet()
                val displayedItems = when (uiState.selectedFilterIndex) {
                    1 -> progress.remainingItems
                    2 -> progress.watchedItems
                    else -> progress.challenge.targetMediaItems
                }

                LazyColumn(
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = innerPadding.calculateBottomPadding() + 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Hero Card with Progress & Illustration
                    item(key = "hero_progress") {
                        ChallengeDetailHeroCard(
                            progress = progress,
                            isJoined = uiState.isJoined,
                            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumLarge)
                        )
                    }

                    // 2. Explainer Guide Banner: How to Make Progress
                    item(key = "guide_explainer") {
                        ChallengeProgressGuideCard(
                            hasMediaItems = progress.challenge.targetMediaItems.isNotEmpty(),
                            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumLarge)
                        )
                    }

                    // 3. Filter Chips (Single-line, non-wrapping)
                    if (progress.challenge.targetMediaItems.isNotEmpty()) {
                        item(key = "filter_chips") {
                            ChallengeFilterRow(
                                totalCount = progress.totalCount,
                                remainingCount = progress.remainingItems.size,
                                watchedCount = progress.watchedItems.size,
                                selectedIndex = uiState.selectedFilterIndex,
                                onFilterSelect = { viewModel.selectFilter(it) }
                            )
                        }
                    }

                    // 4. Add Titles CTA for Custom Goals
                    if (progress.challenge.isCustom) {
                        item(key = "add_titles_to_goal_cta") {
                            Surface(
                                onClick = viewModel::openAddTitlesSearch,
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = MaterialTheme.spacing.mediumLarge)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(
                                        horizontal = MaterialTheme.spacing.mediumLarge,
                                        vertical = MaterialTheme.spacing.smallMedium
                                    )
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.challenges_add_titles_cta),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.challenges_add_titles_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Media Items
                    if (progress.challenge.targetMediaItems.isNotEmpty()) {
                        itemsIndexed(
                            displayedItems,
                            key = { index, item -> "${item.mediaType}_${item.id}_$index" }) { _, item ->
                            val isWatched = watchedIdSet.contains(item.id to item.mediaType)

                            ChallengeMediaListItem(
                                item = item,
                                isWatched = isWatched,
                                isCustom = progress.challenge.isCustom,
                                onClick = {
                                    if (item.mediaType == MediaType.Movie) {
                                        onOpenMovieDetails(item.id)
                                    } else {
                                        onOpenTvShowDetails(item.id)
                                    }
                                },
                                onLogClick = {
                                    viewModel.openLogDialog(item)
                                },
                                onRemoveClick = if (progress.challenge.isCustom) {
                                    { viewModel.requestRemoveItem(item) }
                                } else null,
                                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumLarge)
                            )
                        }
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Text(
                        text = stringResource(R.string.challenges_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Dialogs
    if (uiState.showJoinConfirmation) {
        val title = uiState.progress?.challenge?.title.orEmpty()
        ChallengeJoinConfirmationDialog(
            title = title,
            onConfirm = viewModel::confirmJoin,
            onDismiss = viewModel::dismissJoinConfirmation
        )
    }

    if (uiState.showLeaveConfirmation) {
        val challenge = uiState.progress?.challenge
        val title = challenge?.title.orEmpty()
        val isCustom = challenge?.isCustom == true

        ChallengeLeaveConfirmationDialog(
            title = title,
            isCustom = isCustom,
            onConfirm = {
                if (isCustom) {
                    viewModel.confirmDeleteGoal(onDeleted = onBackClick)
                } else {
                    viewModel.confirmLeave(onLeft = onBackClick)
                }
            },
            onDismiss = viewModel::dismissLeaveConfirmation
        )
    }

    if (uiState.isEditingMetadata) {
        uiState.progress?.challenge?.let { challenge ->
            EditGoalDialog(
                initialTitle = challenge.title,
                initialDescription = challenge.description,
                onDismiss = viewModel::dismissEditMetadataDialog,
                onSave = { newTitle, newDesc ->
                    viewModel.saveMetadata(newTitle, newDesc)
                }
            )
        }
    }

    if (uiState.cannotRemoveLastTitleWarning) {
        CannotRemoveLastTitleDialog(onDismiss = viewModel::dismissCannotRemoveWarning)
    }

    uiState.itemPendingRemoval?.let { item ->
        RemoveMediaItemConfirmDialog(
            item = item,
            onConfirm = viewModel::confirmRemoveItem,
            onDismiss = viewModel::dismissRemoveItem
        )
    }

    if (uiState.isSearchingTitlesToAdd) {
        uiState.progress?.challenge?.let { challenge ->
            Dialog(
                onDismissRequest = viewModel::closeAddTitlesSearch,
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                ChallengeMediaSearchView(
                    searchQuery = uiState.mediaSearchQuery,
                    selectedFilter = uiState.mediaSearchFilter,
                    suggestions = uiState.mediaSearchSuggestions,
                    selectedTitles = challenge.targetMediaItems,
                    isSearching = uiState.isSearchingMedia,
                    onSearchQueryChange = viewModel::onMediaSearchQueryChange,
                    onFilterChange = viewModel::onMediaSearchFilterChange,
                    onClearSearch = viewModel::clearMediaSearch,
                    onToggleMedia = viewModel::toggleMediaInCustomChallenge,
                    onDismiss = viewModel::closeAddTitlesSearch
                )
            }
        }
    }

    uiState.mediaItemToLog?.let { mediaItem ->
        LogAndRateDialog(
            mediaId = mediaItem.id,
            mediaType = mediaItem.mediaType,
            title = mediaItem.title,
            posterImageUrl = mediaItem.posterImageUrl.convertToTmdbPosterUrl(),
            backdropImageUrl = mediaItem.backdropImageUrl.convertToTmdbBackdropUrl(),
            releaseDate = mediaItem.releaseYear,
            tmdbRating = mediaItem.voteAvg,
            onDismiss = viewModel::dismissLogDialog,
            onSave = viewModel::saveDiaryEntry
        )
    }

    if (isShareSheetOpen) {
        uiState.progress?.let { progress ->
            val shareText =
                remember(progress) { viewModel.generateShareableChallengeText(progress) }
            ChallengeShareExportBottomSheet(
                progress = progress,
                shareText = shareText,
                onDismissRequest = { isShareSheetOpen = false }
            )
        }
    }
}
