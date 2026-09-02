package com.ssverma.feature.filter.ui.discovery

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.common.ui.region.RegionSelectionBottomSheet
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.feature.filter.ui.discovery.component.DiscoveryFilterSheet
import com.ssverma.feature.filter.ui.discovery.component.QuickVibesRow
import com.ssverma.feature.filter.ui.discovery.component.SpinTheReelDialog
import com.ssverma.feature.filter.ui.discovery.component.StreamingFilterRow
import com.ssverma.feature.filter.ui.discovery.component.UniversalMediaCard
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.discovery.DiscoveryVibePreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalDiscoveryScreen(
    onBackClick: () -> Unit,
    onOpenMovieDetails: (Int) -> Unit,
    onOpenTvShowDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UniversalDiscoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadNextPage()
        }
    }

    val isScrolled by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0 }
    }
    val headerElevation by animateDpAsState(
        targetValue = if (isScrolled) 4.dp else 0.dp,
        label = "header_elevation"
    )
    val headerColor by animateColorAsState(
        targetValue = if (isScrolled) MaterialTheme.colorScheme.surfaceContainer
        else MaterialTheme.colorScheme.background,
        label = "header_color"
    )

    Scaffold(
        topBar = {
            Surface(
                color = headerColor,
                shadowElevation = headerElevation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Discover & Browse",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.toggleViewMode() }) {
                                Icon(
                                    imageVector = if (uiState.isGridView) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                                    contentDescription = "Toggle View"
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent
                        )
                    )

                    // Symmetrical Movies vs TV Switcher Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                val isMovieSelected = uiState.filter.mediaType == MediaType.Movie
                                val isTvSelected = uiState.filter.mediaType == MediaType.Tv

                                Surface(
                                    onClick = { viewModel.setMediaType(MediaType.Movie) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isMovieSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Movie,
                                            contentDescription = null,
                                            tint = if (isMovieSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Movies",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isMovieSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isMovieSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    onClick = { viewModel.setMediaType(MediaType.Tv) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isTvSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Tv,
                                            contentDescription = null,
                                            tint = if (isTvSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "TV Shows",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isTvSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isTvSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Quick Vibes Carousel
                    QuickVibesRow(
                        selectedVibe = uiState.filter.vibePreset,
                        onVibeSelected = { viewModel.setVibePreset(it) }
                    )

                    // Streaming Subscriptions Bar
                    StreamingFilterRow(
                        watchRegion = uiState.filter.watchRegion,
                        availableProviders = uiState.availableProviders,
                        selectedProviderIds = uiState.filter.selectedProviderIds,
                        onToggleProvider = { viewModel.toggleStreamingProvider(it) },
                        onOpenRegionSheet = { viewModel.openRegionSheet(true) },
                        onOpenFilterSheet = { viewModel.openFilterSheet(true) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.spinRoulette() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Casino,
                        contentDescription = "Roulette",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Roulette",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading && uiState.items.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    ShowTimeLoadingIndicator(modifier = Modifier.size(48.dp))
                }
            } else if (uiState.items.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "No titles found matching your criteria",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try switching vibes, adjusting streaming filters, or changing decade.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.resetFilters() }) {
                        Text("Reset Filters")
                    }
                }
            } else {
                val columns = if (uiState.isGridView) 2 else 1

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    state = gridState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.items, key = { "${it.mediaType}_${it.id}" }) { item ->
                        UniversalMediaCard(
                            item = item,
                            onClick = {
                                if (item.mediaType == MediaType.Movie) {
                                    onOpenMovieDetails(item.id)
                                } else {
                                    onOpenTvShowDetails(item.id)
                                }
                            },
                            onToggleWatchlist = { viewModel.toggleWatchlist(item) },
                            onToggleFavorite = { viewModel.toggleFavorite(item) },
                            onToggleWatched = { viewModel.toggleWatchHistory(item) }
                        )
                    }

                    if (uiState.isLoadingMore) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                ShowTimeLoadingIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Advanced Filters Sheet
    if (uiState.isFilterSheetOpen) {
        DiscoveryFilterSheet(
            filter = uiState.filter,
            onApply = { viewModel.applyFilter(it) },
            onReset = { viewModel.resetFilters() },
            onDismiss = { viewModel.openFilterSheet(false) }
        )
    }

    // Region Selection Sheet
    if (uiState.isRegionSheetOpen) {
        RegionSelectionBottomSheet(
            selectedRegionCode = uiState.filter.watchRegion,
            availableRegions = uiState.availableRegions,
            onRegionSelected = { viewModel.updateRegion(it.iso31661) },
            onDismissRequest = { viewModel.openRegionSheet(false) }
        )
    }

    // Cinema Roulette Dialog
    if (uiState.isRouletteSpinning || uiState.rouletteItem != null) {
        SpinTheReelDialog(
            item = uiState.rouletteItem,
            isSpinning = uiState.isRouletteSpinning,
            onSpinAgain = { viewModel.spinRoulette() },
            onOpenDetails = { type, id ->
                if (type == MediaType.Movie) {
                    onOpenMovieDetails(id)
                } else {
                    onOpenTvShowDetails(id)
                }
            },
            onToggleWatchlist = { viewModel.toggleWatchlist(it) },
            onDismiss = { viewModel.dismissRoulette() }
        )
    }
}
