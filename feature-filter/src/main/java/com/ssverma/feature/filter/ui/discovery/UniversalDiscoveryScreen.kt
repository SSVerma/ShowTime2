package com.ssverma.feature.filter.ui.discovery

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
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.common.ui.region.RegionSelectionBottomSheet
import com.ssverma.core.navigation.dispatcher.IntentDispatcher
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.component.scrolledBottomElevation
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.feature.filter.R
import com.ssverma.feature.filter.ui.discovery.component.DiscoveryFilterSheet
import com.ssverma.feature.filter.ui.discovery.component.QuickVibesRow
import com.ssverma.feature.filter.ui.discovery.component.SpinTheReelDialog
import com.ssverma.feature.filter.ui.discovery.component.StreamingFilterRow
import com.ssverma.feature.filter.ui.discovery.component.UniversalMediaCard
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.library.navigation.LibraryTabDestination
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.showtime.feature.filter.navigation.UniversalDiscoveryNavKey
import kotlinx.coroutines.launch
import com.ssverma.shared.ui.R as SharedUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalDiscoveryScreen(
    onBackClick: () -> Unit,
    onOpenMovieDetails: (Int) -> Unit,
    onOpenTvShowDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    navKey: UniversalDiscoveryNavKey? = null,
    openLibraryPage: (LibraryHomeNavKey) -> Unit = {},
    onOpenCinemaDiary: (() -> Unit)? = null,
    viewModel: UniversalDiscoveryViewModel = hiltViewModel()
) {
    LaunchedEffect(navKey) {
        if (navKey != null) {
            viewModel.initFromNavKey(navKey)
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val density = LocalDensity.current
    var initialHeaderHeight by remember { mutableStateOf<Dp?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UniversalDiscoveryUiEffect.ActionFeedback -> {
                    coroutineScope.launch {
                        val message = context.getString(effect.messageRes)
                        val actionLabel = effect.actionLabelRes?.let { context.getString(it) }
                        val result = snackbarHostState.showImmediateSnackbar(
                            message = message,
                            actionLabel = actionLabel,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed && effect.destination != null) {
                            openLibraryPage(effect.destination)
                        }
                    }
                }
            }
        }
    }

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
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 ||
                    gridState.firstVisibleItemScrollOffset > 0 ||
                    scrollBehavior.state.collapsedFraction > 0f
        }
    }
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        if (scrollBehavior.state.heightOffset == 0f) {
                            initialHeaderHeight = with(density) { coordinates.size.height.toDp() }
                        }
                    }
                    .scrolledBottomElevation(isScrolled = isScrolled)
            ) {
                ShowTimeTopAppBar(
                    title = stringResource(R.string.discover_and_browse),
                    onBackPressed = onBackClick,
                    actions = {
                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                imageVector = if (uiState.isGridView) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                                contentDescription = stringResource(R.string.toggle_view)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    showBottomShadow = false,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        StreamingFilterRow(
                            watchRegion = uiState.filter.watchRegion,
                            availableProviders = uiState.availableProviders,
                            selectedProviderIds = uiState.filter.selectedProviderIds,
                            onToggleProvider = { viewModel.toggleStreamingProvider(it) },
                            onOpenRegionSheet = { viewModel.openRegionSheet(true) },
                            onOpenFilterSheet = { viewModel.openFilterSheet(true) }
                        )

                        if (uiState.isLoading && uiState.items.isNotEmpty()) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.Transparent
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        },
        snackbarHost = { ShowTimeSnackbarHost(hostState = snackbarHostState) },
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
                        contentDescription = stringResource(R.string.roulette),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.roulette),
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
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.items.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ShowTimeLoadingIndicator(modifier = Modifier.size(48.dp))
                }
            } else if (uiState.items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = (initialHeaderHeight
                                ?: innerPadding.calculateTopPadding()) + 4.dp,
                            bottom = innerPadding.calculateBottomPadding() + 16.dp
                        )
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        val isMovieSelected = uiState.filter.mediaType == MediaType.Movie
                        val isTvSelected = uiState.filter.mediaType == MediaType.Tv

                        SegmentedButton(
                            selected = isMovieSelected,
                            onClick = { viewModel.setMediaType(MediaType.Movie) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = {
                                SegmentedButtonDefaults.Icon(active = isMovieSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Movie,
                                        contentDescription = null,
                                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                    )
                                }
                            }
                        ) {
                            Text(stringResource(SharedUiR.string.movies))
                        }

                        SegmentedButton(
                            selected = isTvSelected,
                            onClick = { viewModel.setMediaType(MediaType.Tv) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = {
                                SegmentedButtonDefaults.Icon(active = isTvSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Tv,
                                        contentDescription = null,
                                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                    )
                                }
                            }
                        ) {
                            Text(stringResource(SharedUiR.string.tv_series))
                        }
                    }

                    QuickVibesRow(
                        selectedVibe = uiState.filter.vibePreset,
                        onVibeSelected = { viewModel.setVibePreset(it) },
                        modifier = Modifier
                            .ignoreHorizontalParentPadding(16.dp)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.empty_discovery_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.empty_discovery_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.resetFilters() }) {
                            Text(stringResource(R.string.reset_filters))
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                val columns = if (uiState.isGridView) 2 else 1

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    state = gridState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = (initialHeaderHeight ?: innerPadding.calculateTopPadding()) + 2.dp,
                        bottom = innerPadding.calculateBottomPadding() + 80.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp, bottom = 2.dp)
                        ) {
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            ) {
                                val isMovieSelected = uiState.filter.mediaType == MediaType.Movie
                                val isTvSelected = uiState.filter.mediaType == MediaType.Tv

                                SegmentedButton(
                                    selected = isMovieSelected,
                                    onClick = { viewModel.setMediaType(MediaType.Movie) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                    icon = {
                                        SegmentedButtonDefaults.Icon(active = isMovieSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Movie,
                                                contentDescription = null,
                                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                            )
                                        }
                                    }
                                ) {
                                    Text(stringResource(SharedUiR.string.movies))
                                }

                                SegmentedButton(
                                    selected = isTvSelected,
                                    onClick = { viewModel.setMediaType(MediaType.Tv) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                    icon = {
                                        SegmentedButtonDefaults.Icon(active = isTvSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Tv,
                                                contentDescription = null,
                                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                            )
                                        }
                                    }
                                ) {
                                    Text(stringResource(SharedUiR.string.tv_series))
                                }
                            }

                            QuickVibesRow(
                                selectedVibe = uiState.filter.vibePreset,
                                onVibeSelected = { viewModel.setVibePreset(it) },
                                modifier = Modifier
                                    .ignoreHorizontalParentPadding(16.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }

                    items(uiState.items, key = { "${it.mediaType}_${it.id}" }) { item ->
                        UniversalMediaCard(
                            item = item,
                            isGridView = uiState.isGridView,
                            onClick = {
                                if (item.mediaType == MediaType.Movie) {
                                    onOpenMovieDetails(item.id)
                                } else {
                                    onOpenTvShowDetails(item.id)
                                }
                            },
                            onToggleWatchlist = { viewModel.toggleWatchlist(item) },
                            onToggleFavorite = { viewModel.toggleFavorite(item) },
                            onToggleWatched = { viewModel.toggleWatchHistory(item) },
                            onLogToDiary = onOpenCinemaDiary,
                            onCustomListClick = {
                                val mediaTypeStr =
                                    if (item.mediaType == MediaType.Movie) "movie" else "tv"
                                openLibraryPage(
                                    LibraryHomeNavKey(
                                        initialTab = LibraryTabDestination.CustomLists,
                                        initialMediaType = mediaTypeStr
                                    )
                                )
                            },
                            onOpenDiscussions = {
                                if (item.mediaType == MediaType.Movie) {
                                    onOpenMovieDetails(item.id)
                                } else {
                                    onOpenTvShowDetails(item.id)
                                }
                            },
                            onShare = {
                                val tmdbType =
                                    if (item.mediaType == MediaType.Movie) "movie" else "tv"
                                with(IntentDispatcher) {
                                    context.dispatchShareTextIntent(
                                        "${item.title}\nhttps://www.themoviedb.org/$tmdbType/${item.id}"
                                    )
                                }
                            },
                            onShowFeedback = { message, actionLabel, destination ->
                                coroutineScope.launch {
                                    val result = snackbarHostState.showImmediateSnackbar(
                                        message = message,
                                        actionLabel = actionLabel,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        openLibraryPage(destination ?: LibraryHomeNavKey.Default)
                                    }
                                }
                            }
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

    if (uiState.rouletteItem != null || uiState.isRouletteSpinning) {
        SpinTheReelDialog(
            item = uiState.rouletteItem,
            isSpinning = uiState.isRouletteSpinning,
            onSpinAgain = { viewModel.spinRoulette() },
            onOpenDetails = { mediaType, id ->
                viewModel.dismissRoulette()
                if (mediaType == MediaType.Movie) {
                    onOpenMovieDetails(id)
                } else {
                    onOpenTvShowDetails(id)
                }
            },
            onToggleWatchlist = { item ->
                viewModel.toggleWatchlist(item)
            },
            onDismiss = { viewModel.dismissRoulette() }
        )
    }

    if (uiState.isFilterSheetOpen) {
        DiscoveryFilterSheet(
            filter = uiState.filter,
            onApply = { updatedFilter ->
                viewModel.applyFilter(updatedFilter)
            },
            onReset = {
                viewModel.resetFilters()
            },
            onDismiss = {
                viewModel.openFilterSheet(false)
            }
        )
    }

    if (uiState.isRegionSheetOpen) {
        RegionSelectionBottomSheet(
            selectedRegionCode = uiState.filter.watchRegion,
            availableRegions = uiState.availableRegions,
            onRegionSelected = { region ->
                viewModel.updateRegion(region.iso31661)
            },
            onDismissRequest = {
                viewModel.openRegionSheet(false)
            }
        )
    }
}

private fun Modifier.ignoreHorizontalParentPadding(horizontal: Dp = 16.dp): Modifier =
    layout { measurable, constraints ->
        val paddingPx = horizontal.roundToPx()
        val placeable = measurable.measure(
            constraints.copy(
                maxWidth = constraints.maxWidth + 2 * paddingPx,
                minWidth = constraints.maxWidth + 2 * paddingPx
            )
        )
        layout(constraints.maxWidth, placeable.height) {
            placeable.placeRelative(-paddingPx, 0)
        }
    }

