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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
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
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(challengeId) {
        viewModel.initChallenge(challengeId)
    }

    Scaffold(
        topBar = {
            ShowTimeTopAppBar(
                title = uiState.progress?.challenge?.title ?: "Challenge",
                onBackPressed = onBackClick,
                actions = {
                    uiState.progress?.let { progress ->
                        if (uiState.isJoined) {
                            IconButton(
                                onClick = {
                                    val text = viewModel.generateShareableChallengeText(progress)
                                    context.dispatchShareTextIntent(text)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "Share Progress"
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
                                Text("Leave")
                            }

                            Button(
                                onClick = {
                                    val text = viewModel.generateShareableChallengeText(progress)
                                    context.dispatchShareTextIntent(text)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Progress")
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
                                    text = "Join Challenge",
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
                        bottom = innerPadding.calculateBottomPadding() + 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Hero Card with Progress
                    item(key = "hero_progress") {
                        ChallengeDetailHeroCard(
                            progress = progress,
                            isJoined = uiState.isJoined
                        )
                    }

                    // 2. Explainer Guide Banner: How to Make Progress
                    item(key = "guide_explainer") {
                        ChallengeProgressGuideCard(
                            hasMediaItems = progress.challenge.targetMediaItems.isNotEmpty()
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

                    // 4. Media Items
                    if (progress.challenge.targetMediaItems.isNotEmpty()) {
                        items(displayedItems, key = { "${it.mediaType}_${it.id}" }) { item ->
                            val isWatched = watchedIdSet.contains(item.id to item.mediaType)

                            ChallengeMediaListItem(
                                item = item,
                                isWatched = isWatched,
                                onClick = {
                                    if (item.mediaType == MediaType.Movie) {
                                        onOpenMovieDetails(item.id)
                                    } else {
                                        onOpenTvShowDetails(item.id)
                                    }
                                },
                                onLogClick = {
                                    viewModel.openLogDialog(item)
                                }
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
                        text = "Challenge not found",
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
                Text("Join Challenge?")
            },
            text = {
                Text("Are you ready to take on \"$title\"? Track your progress as you watch and log titles in your Cinema Diary.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmJoin() }
                ) {
                    Text("Join Challenge")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissJoinConfirmation() }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Leave Confirmation Dialog
    if (uiState.showLeaveConfirmation) {
        val title = uiState.progress?.challenge?.title.orEmpty()
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
                Text("Leave Challenge?")
            },
            text = {
                Text("Are you sure you want to leave \"$title\"? Your logged ratings and reviews will remain saved in your Cinema Diary, but this challenge will be removed from your active list.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmLeave(onLeft = onBackClick) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissLeaveConfirmation() }
                ) {
                    Text("Keep Challenge")
                }
            }
        )
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
                        ChallengeCategory.Curated -> "Essential"
                        ChallengeCategory.DirectorSpotlight -> "Director Spotlight"
                        ChallengeCategory.DecadeClassics -> "Decade Classic"
                        ChallengeCategory.GenreSprint -> "Genre Sprint"
                        ChallengeCategory.PersonalGoal -> "Personal Goal"
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
                    text = "${progress.watchedCount} of ${progress.totalCount} completed",
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
                    text = "How to Make Progress",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hasMediaItems) {
                        "Log these titles into your Cinema Diary (rate or review them) as you watch them. Tap \"Log\" on any title below to mark it completed instantly!"
                    } else {
                        "Log any movie or TV show into your Cinema Diary. Each logged entry automatically advances your sprint count towards your personal goal!"
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
    ) {
        FilterChip(
            selected = selectedIndex == 0,
            onClick = { onFilterSelect(0) },
            label = {
                Text(
                    text = "All ($totalCount)",
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
                    text = "Remaining ($remainingCount)",
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
                    text = "Watched ($watchedCount)",
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
    onClick: () -> Unit,
    onLogClick: () -> Unit,
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
                            append(if (item.mediaType == MediaType.Movie) "Movie" else "TV")
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
                            contentDescription = "Watched",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Watched",
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
                        text = "Log",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
