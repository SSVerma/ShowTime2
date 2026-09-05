package com.ssverma.feature.library.ui.taste

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.navigation.dispatcher.IntentDispatcher
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.feature.library.R
import com.ssverma.feature.library.ui.diary.component.DiaryFilterRow
import com.ssverma.feature.library.ui.taste.component.CinephilePersonaCard
import com.ssverma.feature.library.ui.taste.component.TasteEraDistributionCard
import com.ssverma.feature.library.ui.taste.component.TasteKeyMetricsRow
import com.ssverma.feature.library.ui.taste.component.TasteRatingHistogram
import com.ssverma.feature.library.ui.taste.component.TasteRecommendationShelfRow
import com.ssverma.feature.library.ui.taste.component.TasteRecommendationsHeroCard
import com.ssverma.shared.domain.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasteProfileScreen(
    onBackClick: () -> Unit,
    onOpenMovieDetails: (Int) -> Unit,
    onOpenTvShowDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasteProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            ShowTimeTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.taste_profile_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.taste_profile_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onBackPressed = onBackClick,
                actions = {
                    IconButton(onClick = {
                        val shareText = viewModel.getShareTasteText(uiState.stats)
                        with(IntentDispatcher) {
                            context.dispatchShareTextIntent(text = shareText)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.taste_share_cd)
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
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            // Filter Chips Row
            item {
                DiaryFilterRow(
                    activeFilter = uiState.selectedFilter,
                    onFilterSelected = viewModel::setFilter,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (uiState.stats.totalItemsLogged == 0) {
                item {
                    EmptyTasteProfileState(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                    )
                }
            } else {
                // Cinephile Persona Card
                item {
                    CinephilePersonaCard(
                        persona = uiState.stats.persona,
                        onShareClick = {
                            val shareText = viewModel.getShareTasteText(uiState.stats)
                            with(IntentDispatcher) {
                                context.dispatchShareTextIntent(text = shareText)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Key Metrics (Watch time, items, avg rating, rewatches)
                item {
                    TasteKeyMetricsRow(
                        stats = uiState.stats,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                // Rating Histogram
                if (uiState.stats.ratingDistribution.isNotEmpty()) {
                    item {
                        TasteRatingHistogram(
                            ratings = uiState.stats.ratingDistribution,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                // Era Spectrum
                if (uiState.stats.eraDistribution.isNotEmpty()) {
                    item {
                        TasteEraDistributionCard(
                            eras = uiState.stats.eraDistribution,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Recommendations Hero Banner
            if (uiState.recommendationShelves.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    TasteRecommendationsHeroCard(
                        isRefreshing = uiState.isRefreshingRecommendations,
                        onRefreshClick = viewModel::refreshRecommendations,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Recommendation Shelves
                items(
                    count = uiState.recommendationShelves.size,
                    key = { uiState.recommendationShelves[it].id }
                ) { index ->
                    val shelf = uiState.recommendationShelves[index]
                    TasteRecommendationShelfRow(
                        shelf = shelf,
                        onMediaClick = { mediaItem ->
                            if (mediaItem.mediaType == MediaType.Movie) {
                                onOpenMovieDetails(mediaItem.id)
                            } else {
                                onOpenTvShowDetails(mediaItem.id)
                            }
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun EmptyTasteProfileState(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.taste_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.taste_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
