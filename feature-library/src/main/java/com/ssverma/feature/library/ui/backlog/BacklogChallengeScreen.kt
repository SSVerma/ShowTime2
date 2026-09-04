package com.ssverma.feature.library.ui.backlog

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.feature.library.ui.backlog.component.ActiveChallengeCard
import com.ssverma.feature.library.ui.backlog.component.BacklogHeroIntroCard
import com.ssverma.feature.library.ui.backlog.component.BlindspotRadarSection
import com.ssverma.feature.library.ui.backlog.component.ChallengeHeroProgressCard
import com.ssverma.feature.library.ui.backlog.component.CreateChallengeBottomSheet
import com.ssverma.feature.library.ui.backlog.component.CuratedChallengeShelf
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.CinephileChallenge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BacklogChallengeScreen(
    onBackPressed: () -> Unit,
    onOpenMovieDetails: (Int) -> Unit,
    onOpenTvShowDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onOpenChallengeDetail: (String) -> Unit = {},
    viewModel: BacklogChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var challengeToJoin by remember { mutableStateOf<CinephileChallenge?>(null) }

    val handleShare: (String) -> Unit = { text ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Challenge Progress"))
    }

    Scaffold(
        topBar = {
            ShowTimeTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Backlog & Challenges",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Track goals, retrospectives & blindspots",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onBackPressed = onBackPressed,
                actions = {
                    IconButton(onClick = viewModel::openCreateCustomGoalSheet) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Create Custom Goal"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Active Challenges Section or Intro Banner
            if (uiState.activeChallenges.isNotEmpty()) {
                item(key = "active_challenges_section") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Active Challenges (${uiState.activeChallenges.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = uiState.activeChallenges,
                                key = { it.challenge.id }
                            ) { progress ->
                                val cardModifier = if (uiState.activeChallenges.size == 1) {
                                    Modifier.fillParentMaxWidth()
                                } else {
                                    Modifier.width(285.dp)
                                }
                                ActiveChallengeCard(
                                    progress = progress,
                                    onClick = {
                                        viewModel.openChallengeDetail(progress)
                                        onOpenChallengeDetail(progress.challenge.id)
                                    },
                                    modifier = cardModifier
                                )
                            }
                        }
                    }
                }
            } else {
                item(key = "intro_hero_card") {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BacklogHeroIntroCard(
                            curatedCount = uiState.curatedChallenges.size,
                            onCreateGoalClick = viewModel::openCreateCustomGoalSheet
                        )
                    }
                }
            }

            // 2. Priority Blindspots Radar
            item(key = "blindspots_radar") {
                BlindspotRadarSection(
                    blindspots = uiState.blindspots,
                    onOpenMovieDetails = onOpenMovieDetails,
                    onOpenTvShowDetails = onOpenTvShowDetails,
                    onRemoveBlindspot = viewModel::removeBlindspot
                )
            }

            // 3. Curated Challenges Shelf
            item(key = "curated_challenges_shelf") {
                CuratedChallengeShelf(
                    curatedChallenges = uiState.curatedChallenges,
                    activeChallengeIds = uiState.activeChallenges.map { it.challenge.id }
                        .toSet(),
                    onJoinChallenge = { challenge ->
                        challengeToJoin = challenge
                    },
                    onOpenChallengeDetail = { challenge ->
                        onOpenChallengeDetail(challenge.id)
                    }
                )
            }
        }
    }

    // Join Challenge Confirmation Dialog
    challengeToJoin?.let { challenge ->
        AlertDialog(
            onDismissRequest = { challengeToJoin = null },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(text = "Join Challenge?")
            },
            text = {
                Text(
                    text = "Start tracking your progress for \"${challenge.title}\". You can log titles via your Cinema Diary to complete this challenge."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toJoin = challenge
                        challengeToJoin = null
                        viewModel.joinCuratedChallenge(toJoin)
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Joined \"${toJoin.title}\"!",
                                actionLabel = "View",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onOpenChallengeDetail(toJoin.id)
                            }
                        }
                    }
                ) {
                    Text(text = "Join Challenge")
                }
            },
            dismissButton = {
                TextButton(onClick = { challengeToJoin = null }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    // Modal Sheet: Create Personal Goal
    if (uiState.isCreatingCustomGoal) {
        CreateChallengeBottomSheet(
            searchQuery = uiState.mediaSearchQuery,
            searchSuggestions = uiState.mediaSearchSuggestions,
            isSearching = uiState.isSearchingMedia,
            onSearchQueryChange = viewModel::onMediaSearchQueryChange,
            onClearSearch = viewModel::clearMediaSearch,
            onDismiss = viewModel::closeCreateCustomGoalSheet,
            onCreateGoal = viewModel::createCustomGoal
        )
    }
}
