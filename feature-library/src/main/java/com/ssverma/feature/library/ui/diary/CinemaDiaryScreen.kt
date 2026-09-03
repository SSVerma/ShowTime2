package com.ssverma.feature.library.ui.diary

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.feature.library.ui.diary.component.DiaryStatsHeader
import com.ssverma.feature.library.ui.diary.component.DiaryTimelineItemCard
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.ui.component.diary.LogAndRateDialog

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

    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val shadowAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 0.20f else 0f,
        label = "diary_top_shadow_alpha"
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Cinema Diary",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Personal ratings & viewing log",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )

                if (shadowAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = shadowAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
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
                    item {
                        DiaryStatsHeader(
                            stats = uiState.stats,
                            onOpenTasteProfile = onOpenTasteProfile,
                            onOpenWrapped = onOpenWrapped,
                            onOpenChallenges = onOpenChallenges
                        )
                    }
                }

                // Filter Row
                item {
                    val filterScrollState = rememberScrollState()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(filterScrollState)
                            .padding(vertical = 4.dp)
                    ) {
                        DiaryFilterType.entries.forEach { filter ->
                            val (label, icon) = when (filter) {
                                DiaryFilterType.ALL -> "All" to Icons.Rounded.AutoAwesome
                                DiaryFilterType.MOVIES_ONLY -> "Movies" to Icons.Rounded.Movie
                                DiaryFilterType.TV_ONLY -> "TV Shows" to Icons.Rounded.Tv
                                DiaryFilterType.REWATCHES_ONLY -> "Rewatches" to Icons.Rounded.Replay
                                DiaryFilterType.FIVE_STARS_ONLY -> "5-Stars" to Icons.Rounded.Star
                            }
                            val isSelected = uiState.activeFilter == filter

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setFilter(filter) },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Empty State
                if (uiState.timelineGroups.isEmpty()) {
                    item {
                        DiaryEmptyView(activeFilter = uiState.activeFilter)
                    }
                } else {
                    // Timeline Groups
                    uiState.timelineGroups.forEach { group ->
                        item {
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
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        items(group.entries, key = { it.id }) { entry ->
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
                                onShare = { shareDiaryEntry(context, entry) }
                            )
                        }
                    }
                }
            }
        }
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
                    text = "Delete Diary Entry?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${entry.title}\" from your Cinema Diary?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmDeleteEntry() }) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDelete() }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
private fun DiaryEmptyView(
    activeFilter: DiaryFilterType,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.HistoryEdu,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(18.dp)
                    .size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (activeFilter == DiaryFilterType.ALL) "Your Cinema Diary is Empty" else "No Matching Diary Logs",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (activeFilter == DiaryFilterType.ALL)
                "Log movies & TV shows from their detail pages to build your personal viewing timeline with star ratings and reviews."
            else
                "Try selecting a different filter above to view other diary logs.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun shareDiaryEntry(context: Context, entry: DiaryEntry) {
    val shareText = buildString {
        append("🎬 ")
        append(entry.title)
        if (entry.releaseDate.isNotBlank()) {
            append(" (${entry.releaseDate.take(4)})")
        }
        append("\n⭐ Personal Rating: %.1f / 5.0".format(entry.userRating))
        if (entry.isRewatch) {
            append(" (Rewatch 🔁)")
        }
        if (entry.review.isNotBlank()) {
            append("\n\n\"${entry.review}\"")
        }
        append("\n\nLogged via ShowTime ✨")
    }

    context.dispatchShareTextIntent(text = shareText)
}
