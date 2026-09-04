package com.ssverma.feature.library.ui.wrapped

import android.content.Intent
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.feature.library.ui.wrapped.component.MilestoneDetailBottomSheet
import com.ssverma.feature.library.ui.wrapped.component.WrappedHeroCard
import com.ssverma.feature.library.ui.wrapped.component.WrappedMilestonesGrid
import com.ssverma.feature.library.ui.wrapped.component.WrappedMonthlyTimeline
import com.ssverma.feature.library.ui.wrapped.component.WrappedTopFavoritesGrid
import com.ssverma.shared.domain.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinephileWrappedScreen(
    onBackPressed: () -> Unit,
    onOpenMovieDetails: (movieId: Int) -> Unit,
    onOpenTvShowDetails: (tvShowId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CinephileWrappedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val onShareWrapped = {
        uiState.summary?.let { summary ->
            val shareText = viewModel.generateWrappedShareText(summary)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Cinema Wrapped")
            context.startActivity(shareIntent)
        }
        Unit
    }

    Scaffold(
        topBar = {
            ShowTimeTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Cinema Wrapped",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Your annual & all-time cinema journey",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onBackPressed = onBackPressed,
                actions = {
                    IconButton(onClick = onShareWrapped) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share"
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
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        if (uiState.isLoading || uiState.summary == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val summary = uiState.summary!!

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 4.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Year Selector Chips
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
                    ) {
                        uiState.availableYears.forEach { year ->
                            val label = if (year == 0) "All-Time" else "$year"
                            val isSelected = uiState.selectedYear == year

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onSelectYear(year) },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (year == 0) Icons.Rounded.AutoAwesome else Icons.Rounded.CalendarToday,
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

                // Hero Story Card
                item {
                    WrappedHeroCard(
                        summary = summary,
                        onShareClick = onShareWrapped,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Top Favorites
                item {
                    WrappedTopFavoritesGrid(
                        topEntries = summary.topRatedMedia,
                        onMediaClick = { entry ->
                            if (entry.mediaType == MediaType.Movie) {
                                onOpenMovieDetails(entry.mediaId)
                            } else {
                                onOpenTvShowDetails(entry.mediaId)
                            }
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Monthly Rhythm Timeline
                item {
                    WrappedMonthlyTimeline(
                        monthlyDistribution = summary.monthlyDistribution,
                        mostActiveMonth = summary.mostActiveMonth,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Milestones & Badges Grid
                item {
                    WrappedMilestonesGrid(
                        milestones = summary.milestones,
                        onMilestoneClick = { milestone ->
                            viewModel.onSelectMilestone(milestone)
                        },
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            }
        }
    }

    // Milestone Detail Bottom Sheet
    uiState.selectedMilestone?.let { milestone ->
        MilestoneDetailBottomSheet(
            milestone = milestone,
            onDismiss = { viewModel.onSelectMilestone(null) }
        )
    }
}

