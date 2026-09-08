package com.ssverma.feature.library.ui.backlog.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
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
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.feature.library.R
import com.ssverma.feature.library.ui.backlog.component.ChallengeMediaSearchView
import com.ssverma.feature.library.ui.backlog.component.ChallengeShareExportBottomSheet
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeProgress
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
                            IconButton(
                                onClick = { viewModel.openEditMetadataDialog() }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = stringResource(R.string.challenges_edit_goal_title)
                                )
                            }
                        }
                        if (uiState.isJoined) {
                            IconButton(
                                onClick = { isShareSheetOpen = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = stringResource(R.string.challenges_share_card_btn)
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
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (uiState.isJoined) {
                            OutlinedButton(
                                onClick = { viewModel.requestLeaveConfirmation() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (progress.challenge.isCustom) {
                                        stringResource(R.string.challenges_delete_goal_btn)
                                    } else {
                                        stringResource(R.string.challenges_leave_action)
                                    }
                                )
                            }

                            Button(
                                onClick = { isShareSheetOpen = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.challenges_share_card_btn))
                            }
                        } else {
                            Button(
                                onClick = { viewModel.requestJoinConfirmation() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.challenges_join_cta),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
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
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Hero Card with Progress
                    item(key = "hero_progress") {
                        ChallengeDetailHeroCard(
                            progress = progress,
                            isJoined = uiState.isJoined,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // 2. Explainer Guide Banner: How to Make Progress
                    item(key = "guide_explainer") {
                        ChallengeProgressGuideCard(
                            hasMediaItems = progress.challenge.targetMediaItems.isNotEmpty(),
                            modifier = Modifier.padding(horizontal = 16.dp)
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
                                onClick = { viewModel.openAddTitlesSearch() },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
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
                                    Spacer(modifier = Modifier.width(14.dp))
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
                                modifier = Modifier.padding(horizontal = 16.dp)
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

    // Join Confirmation Dialog
    if (uiState.showJoinConfirmation) {
        val title = uiState.progress?.challenge?.title.orEmpty()
        AlertDialog(
            onDismissRequest = { viewModel.dismissJoinConfirmation() },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.challenges_join_dialog_title))
            },
            text = {
                Text(stringResource(R.string.challenges_join_dialog_msg, title))
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmJoin() }
                ) {
                    Text(stringResource(R.string.challenges_join_cta))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissJoinConfirmation() }
                ) {
                    Text(stringResource(R.string.challenges_action_cancel))
                }
            }
        )
    }

    // Leave or Delete Confirmation Dialog
    if (uiState.showLeaveConfirmation) {
        val challenge = uiState.progress?.challenge
        val title = challenge?.title.orEmpty()
        val isCustom = challenge?.isCustom == true

        AlertDialog(
            onDismissRequest = { viewModel.dismissLeaveConfirmation() },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = if (isCustom) {
                        stringResource(R.string.challenges_delete_goal_title)
                    } else {
                        stringResource(R.string.challenges_leave_dialog_title)
                    }
                )
            },
            text = {
                Text(
                    text = if (isCustom) {
                        stringResource(R.string.challenges_delete_goal_msg, title)
                    } else {
                        stringResource(R.string.challenges_leave_dialog_msg, title)
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isCustom) {
                            viewModel.confirmDeleteGoal(onDeleted = onBackClick)
                        } else {
                            viewModel.confirmLeave(onLeft = onBackClick)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = if (isCustom) {
                            stringResource(R.string.challenges_delete_goal_confirm)
                        } else {
                            stringResource(R.string.challenges_leave_action)
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissLeaveConfirmation() }
                ) {
                    Text(
                        text = if (isCustom) {
                            stringResource(R.string.cancel)
                        } else {
                            stringResource(R.string.challenges_keep_challenge_action)
                        }
                    )
                }
            }
        )
    }

    // Edit Goal Metadata Dialog
    if (uiState.isEditingMetadata) {
        uiState.progress?.challenge?.let { challenge ->
            EditGoalDialog(
                initialTitle = challenge.title,
                initialDescription = challenge.description,
                onDismiss = { viewModel.dismissEditMetadataDialog() },
                onSave = { newTitle, newDesc ->
                    viewModel.saveMetadata(newTitle, newDesc)
                }
            )
        }
    }

    // Remove Item Confirmation Dialog
    uiState.itemPendingRemoval?.let { item ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissRemoveItem() },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(stringResource(R.string.challenges_remove_title_from_goal_title))
            },
            text = {
                Text(stringResource(R.string.challenges_remove_title_from_goal_msg, item.title))
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmRemoveItem() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.remove_item))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRemoveItem() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Cannot Remove Last Title Warning Dialog
    if (uiState.cannotRemoveLastTitleWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCannotRemoveWarning() },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.challenges_cannot_remove_last_title))
            },
            text = {
                Text(stringResource(R.string.challenges_cannot_remove_last_title_msg))
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissCannotRemoveWarning() }) {
                    Text(stringResource(R.string.challenges_cannot_remove_last_title_ok))
                }
            }
        )
    }

    // Full-Screen Search Dialog to Add Titles to Custom Goal
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

    // Direct In-Context Diary Log Dialog
    uiState.mediaItemToLog?.let { mediaItem ->
        LogAndRateDialog(
            mediaId = mediaItem.id,
            mediaType = mediaItem.mediaType,
            title = mediaItem.title,
            posterImageUrl = mediaItem.posterImageUrl.convertToTmdbPosterUrl(),
            backdropImageUrl = mediaItem.backdropImageUrl.convertToTmdbBackdropUrl(),
            releaseDate = mediaItem.releaseYear,
            tmdbRating = mediaItem.voteAvg,
            onDismiss = { viewModel.dismissLogDialog() },
            onSave = { entry -> viewModel.saveDiaryEntry(entry) }
        )
    }

    // Aesthetic Visual Share Bottom Sheet
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

@Composable
private fun ChallengeDetailHeroCard(
    progress: ChallengeProgress,
    isJoined: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Badges Row: Category & Milestone
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    val catLabel = when (progress.challenge.category) {
                        ChallengeCategory.Curated -> stringResource(R.string.challenges_category_essential)
                        ChallengeCategory.DirectorSpotlight -> stringResource(R.string.challenges_category_director)
                        ChallengeCategory.DecadeClassics -> stringResource(R.string.challenges_category_decade)
                        ChallengeCategory.GenreSprint -> stringResource(R.string.challenges_category_genre)
                        ChallengeCategory.PersonalGoal -> stringResource(R.string.challenges_category_goal)
                    }
                    Text(
                        text = catLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = progress.milestoneTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title and Description
            Text(
                text = progress.challenge.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (progress.challenge.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = progress.challenge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Progress Metrics
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(
                        R.string.challenges_progress_completed,
                        progress.watchedCount,
                        progress.totalCount
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${progress.progressPercentage}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.progressPercentage / 100f },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun ChallengeProgressGuideCard(
    hasMediaItems: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = stringResource(R.string.challenges_guide_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hasMediaItems) {
                        stringResource(R.string.challenges_guide_with_titles)
                    } else {
                        stringResource(R.string.challenges_guide_without_titles)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChallengeFilterRow(
    totalCount: Int,
    remainingCount: Int,
    watchedCount: Int,
    selectedIndex: Int,
    onFilterSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        FilterChip(
            selected = selectedIndex == 0,
            onClick = { onFilterSelect(0) },
            label = {
                Text(
                    text = stringResource(R.string.challenges_filter_all_count, totalCount),
                    maxLines = 1,
                    softWrap = false
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        FilterChip(
            selected = selectedIndex == 1,
            onClick = { onFilterSelect(1) },
            label = {
                Text(
                    text = stringResource(
                        R.string.challenges_filter_remaining_count,
                        remainingCount
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        FilterChip(
            selected = selectedIndex == 2,
            onClick = { onFilterSelect(2) },
            label = {
                Text(
                    text = stringResource(R.string.challenges_filter_watched_count, watchedCount),
                    maxLines = 1,
                    softWrap = false
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

@Composable
private fun ChallengeMediaListItem(
    item: ChallengeMediaItem,
    isWatched: Boolean,
    isCustom: Boolean,
    onClick: () -> Unit,
    onLogClick: () -> Unit,
    onRemoveClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWatched) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            if (isWatched) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Poster thumbnail
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(width = 54.dp, height = 78.dp)
            ) {
                NetworkImage(
                    url = item.posterImageUrl.convertToTmdbPosterUrl(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.mediaType == MediaType.Movie) {
                            Icons.Rounded.Movie
                        } else {
                            Icons.Rounded.Tv
                        },
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = buildString {
                            append(
                                if (item.mediaType == MediaType.Movie) {
                                    stringResource(R.string.media_type_movie)
                                } else {
                                    stringResource(R.string.media_type_tv_short)
                                }
                            )
                            if (item.releaseYear.isNotBlank()) {
                                append(" • ")
                                append(item.releaseYear)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.directorOrCreator.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.directorOrCreator,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.voteAvg > 0f) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format("%.1f", item.voteAvg),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action / Status Indicator
            if (isWatched) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = stringResource(R.string.challenges_completed_label),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.challenges_completed_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                FilledTonalButton(
                    onClick = onLogClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditCalendar,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.diary_fab_log),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isCustom && onRemoveClick != null) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.remove_item),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditGoalDialog(
    initialTitle: String,
    initialDescription: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.challenges_edit_goal_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.challenges_edit_goal_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.challenges_edit_goal_desc)) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.trim(), description.trim()) },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.challenges_edit_goal_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
