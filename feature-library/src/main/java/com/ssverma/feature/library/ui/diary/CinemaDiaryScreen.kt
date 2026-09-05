package com.ssverma.feature.library.ui.diary

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.feature.library.R
import com.ssverma.feature.library.ui.diary.component.DiaryEmptyView
import com.ssverma.feature.library.ui.diary.component.DiaryFilterRow
import com.ssverma.feature.library.ui.diary.component.DiaryStatsHeader
import com.ssverma.feature.library.ui.diary.component.DiaryTimelineItemCard
import com.ssverma.feature.library.ui.diary.component.LogMediaSearchView
import com.ssverma.feature.library.ui.diary.util.DiaryShareHelper
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.ui.component.diary.LogAndRateDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaDiaryScreen(
    onBackClick: () -> Unit,
    onOpenMovieDetails: (Int) -> Unit,
    onOpenTvShowDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onOpenTasteProfile: () -> Unit = {},
    onOpenWrapped: () -> Unit = {},
    onOpenChallenges: () -> Unit = {},
    viewModel: CinemaDiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                ShowTimeTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.cinema_diary),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.cinema_diary_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onBackPressed = onBackClick,
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (!uiState.isSearchingToLog) {
                    FloatingActionButton(
                        onClick = { viewModel.onOpenLogSearch() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp
                        ),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(R.string.diary_fab_log_cd),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.diary_fab_log),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 4.dp,
                        bottom = paddingValues.calculateBottomPadding() + 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Stats
                    if (uiState.stats.totalLogged > 0) {
                        item(key = "diary_stats_header", contentType = "header") {
                            DiaryStatsHeader(
                                stats = uiState.stats,
                                onOpenTasteProfile = onOpenTasteProfile,
                                onOpenWrapped = onOpenWrapped,
                                onOpenChallenges = onOpenChallenges
                            )
                        }
                    }

                    // Filter Row
                    item(key = "diary_filters", contentType = "filters") {
                        DiaryFilterRow(
                            activeFilter = uiState.activeFilter,
                            onFilterSelected = viewModel::setFilter
                        )
                    }

                    // Empty State
                    if (uiState.timelineGroups.isEmpty()) {
                        item(key = "diary_empty_view", contentType = "empty") {
                            DiaryEmptyView(
                                activeFilter = uiState.activeFilter,
                                onLogClick = { viewModel.onOpenLogSearch() }
                            )
                        }
                    } else {
                        // Timeline Groups
                        uiState.timelineGroups.forEach { group ->
                            item(
                                key = "header_${group.monthYearLabel}",
                                contentType = "month_header"
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = group.monthYearLabel.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }

                            items(
                                items = group.entries,
                                key = { "entry_${it.id}" },
                                contentType = { "diary_entry" }
                            ) { entry ->
                                DiaryTimelineItemCard(
                                    entry = entry,
                                    onClick = {
                                        if (entry.mediaType == MediaType.Tv) {
                                            onOpenTvShowDetails(entry.mediaId)
                                        } else {
                                            onOpenMovieDetails(entry.mediaId)
                                        }
                                    },
                                    onEdit = { viewModel.onEditEntry(entry) },
                                    onDelete = { viewModel.onRequestDeleteEntry(entry) },
                                    onShare = { DiaryShareHelper.shareDiaryEntry(context, entry) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full-Screen Search View to Log Title
        AnimatedVisibility(
            visible = uiState.isSearchingToLog,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 6 })
        ) {
            LogMediaSearchView(
                searchQuery = uiState.mediaSearchQuery,
                selectedFilter = uiState.mediaSearchFilter,
                suggestions = uiState.mediaSearchSuggestions,
                isSearching = uiState.isSearchingMedia,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onClearSearch = viewModel::onClearSearch,
                onMediaSelected = viewModel::onSelectMediaToLog,
                onDismiss = viewModel::onDismissLogSearch
            )
        }
    }

    // Direct In-Context Log Dialog for Selected Search Title
    uiState.mediaItemPendingLog?.let { mediaItem ->
        LogAndRateDialog(
            mediaId = mediaItem.id,
            mediaType = mediaItem.mediaType,
            title = mediaItem.title,
            posterImageUrl = mediaItem.posterImageUrl,
            backdropImageUrl = mediaItem.backdropImageUrl,
            releaseDate = mediaItem.releaseYear,
            tmdbRating = mediaItem.voteAvg,
            onDismiss = { viewModel.onDismissLogDialog() },
            onSave = { entry ->
                viewModel.onSaveNewEntry(entry)
                coroutineScope.launch {
                    snackbarHostState.showImmediateSnackbar(
                        message = context.getString(R.string.diary_logged_success, mediaItem.title),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    // Edit Dialog
    uiState.entryPendingEdit?.let { entry ->
        LogAndRateDialog(
            mediaId = entry.mediaId,
            mediaType = entry.mediaType,
            title = entry.title,
            posterImageUrl = entry.posterImageUrl,
            backdropImageUrl = entry.backdropImageUrl,
            releaseDate = entry.releaseDate,
            tmdbRating = entry.tmdbRating,
            existingEntry = entry,
            onDismiss = { viewModel.onDismissEdit() },
            onSave = { updatedEntry -> viewModel.onSaveEditedEntry(updatedEntry) }
        )
    }

    // Delete Confirmation Dialog
    uiState.entryPendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            title = {
                Text(
                    text = stringResource(R.string.diary_delete_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.diary_delete_dialog_msg, entry.title),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmDeleteEntry() }) {
                    Text(
                        text = stringResource(R.string.diary_delete_action),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDelete() }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}
