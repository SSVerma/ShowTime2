package com.ssverma.feature.library.ui.backlog

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.feature.library.ui.backlog.component.ActiveChallengeCard
import com.ssverma.feature.library.ui.backlog.component.BlindspotRadarSection
import com.ssverma.feature.library.ui.backlog.component.ChallengeDetailBottomSheet
import com.ssverma.feature.library.ui.backlog.component.ChallengeHeroProgressCard
import com.ssverma.feature.library.ui.backlog.component.CreateChallengeBottomSheet
import com.ssverma.feature.library.ui.backlog.component.CuratedChallengeShelf
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.ChallengeCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BacklogChallengeScreen(
    onBackPressed: () -> Unit,
    onOpenMovieDetails: (Int) -> Unit,
    onOpenTvShowDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BacklogChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()

    val handleShare: (String) -> Unit = { text ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Challenge Progress"))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backlog & Challenges",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::openCreateCustomGoalSheet) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Create Custom Goal"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Primary Highlight Hero Card
                val heroChallenge = uiState.activeChallenges.firstOrNull()
                if (heroChallenge != null) {
                    item(key = "hero_challenge_card") {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ChallengeHeroProgressCard(
                                progress = heroChallenge,
                                onViewBreakdown = { viewModel.openChallengeDetail(heroChallenge) },
                                onShare = handleShare
                            )
                        }
                    }
                }

                // 2. Active Challenges Carousel / List
                if (uiState.activeChallenges.size > 1) {
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
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(
                                    items = uiState.activeChallenges.drop(1),
                                    key = { it.challenge.id }
                                ) { challengeProgress ->
                                    ActiveChallengeCard(
                                        progress = challengeProgress,
                                        onClick = { viewModel.openChallengeDetail(challengeProgress) },
                                        modifier = Modifier.width(260.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Priority Blindspots Radar
                item(key = "blindspots_radar") {
                    BlindspotRadarSection(
                        blindspots = uiState.blindspots,
                        onOpenMovieDetails = onOpenMovieDetails,
                        onOpenTvShowDetails = onOpenTvShowDetails,
                        onRemoveBlindspot = viewModel::removeBlindspot
                    )
                }

                // 4. Curated Challenges Catalog
                item(key = "curated_challenges_shelf") {
                    CuratedChallengeShelf(
                        curatedChallenges = uiState.curatedChallenges,
                        activeChallengeIds = uiState.activeChallenges.map { it.challenge.id }
                            .toSet(),
                        onJoinChallenge = viewModel::joinCuratedChallenge
                    )
                }
            }
        }
    }

    // Modal Sheet: Challenge Breakdown Details
    uiState.selectedChallengeDetail?.let { detail ->
        ChallengeDetailBottomSheet(
            progress = detail,
            onDismiss = viewModel::closeChallengeDetail,
            onOpenMovieDetails = onOpenMovieDetails,
            onOpenTvShowDetails = onOpenTvShowDetails,
            onLeaveChallenge = viewModel::leaveChallenge,
            onShare = handleShare
        )
    }

    // Modal Sheet: Create Personal Goal
    if (uiState.isCreatingCustomGoal) {
        CreateChallengeBottomSheet(
            onDismiss = viewModel::closeCreateCustomGoalSheet,
            onCreateGoal = viewModel::createCustomGoal
        )
    }
}
