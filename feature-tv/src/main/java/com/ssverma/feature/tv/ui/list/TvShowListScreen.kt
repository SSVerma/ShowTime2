package com.ssverma.feature.tv.ui.list

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.tv.analytics.TvAnalyticsEvent
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.feature.tv.analytics.TvAnalyticsValues
import com.ssverma.feature.tv.analytics.asAnalyticsListingType
import com.ssverma.feature.tv.ui.filter.TvFiltersScreen
import com.ssverma.feature.tv.ui.list.component.TvShowListTopBar
import com.ssverma.feature.tv.ui.list.content.TvShowsGridContent
import com.ssverma.feature.tv.ui.list.content.TvShowsListContent
import com.ssverma.shared.domain.model.ProviderInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TvShowListScreen(
    onBackPressed: () -> Unit,
    openTvShowDetails: (Int) -> Unit,
    openWatchHub: (providerInfo: ProviderInfo) -> Unit,
    openLibraryPage: (LibraryHomeNavKey) -> Unit = {},
    viewModel: TvShowListViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrackScreenView(
        screenName = TvAnalyticsScreenName.TV_LISTING,
        screenClass = uiState.config.asAnalyticsListingType()
    )

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        val analytics = LocalAnalytics.current

        val tvShowPagingItems = viewModel.pagedTvShows.collectAsLazyPagingItems()

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        AppPage(
            scrollBehavior = scrollBehavior,
            snackbarHost = { ShowTimeSnackbarHost(hostState = snackbarHostState) },
            topBar = { behavior ->
                TvShowListTopBar(
                    uiState = uiState,
                    onToggleViewMode = { viewModel.toggleViewMode() },
                    onOpenFilters = {
                        analytics.logEvent(
                            TvAnalyticsEvent.FilterClicked(
                                listingType = uiState.config.asAnalyticsListingType()
                            )
                        )
                        coroutineScope.launch { sheetState.show() }
                    },
                    onBackPressed = onBackPressed,
                    scrollBehavior = behavior
                )
            }
        ) { innerPadding ->

            PagedContent(pagingItems = tvShowPagingItems) { items ->
                Crossfade(uiState.isGridView, label = "TvShowListViewModeTransition") { isGrid ->
                    val onShowFeedback: (String, String?, LibraryHomeNavKey?) -> Unit =
                        { message, actionLabel, destination ->
                            coroutineScope.launch {
                                val result = snackbarHostState.showImmediateSnackbar(
                                    message = message,
                                    actionLabel = actionLabel,
                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                )
                                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                    openLibraryPage(destination ?: LibraryHomeNavKey.Default)
                                }
                            }
                        }

                    if (isGrid) {
                        TvShowsGridContent(
                            tvShowPagingItems = items,
                            config = uiState.config,
                            openTvShowDetails = { tvShow ->
                                analytics.logEvent(
                                    TvAnalyticsEvent.TvShowClicked(
                                        tvShow = tvShow,
                                        section = TvAnalyticsValues.SECTION_LISTING_GRID,
                                        sourceScreen = TvAnalyticsScreenName.TV_LISTING,
                                    )
                                )
                                openTvShowDetails(tvShow.id)
                            },
                            onWatchProviderClick = { provider ->
                                analytics.logEvent(
                                    TvAnalyticsEvent.WatchProviderClicked(
                                        providerInfo = provider,
                                        sourceScreen = TvAnalyticsScreenName.TV_LISTING
                                    )
                                )
                                openWatchHub(provider)
                            },
                            onShowFeedback = onShowFeedback,
                            modifier = Modifier.padding(innerPadding),
                        )
                    } else {
                        TvShowsListContent(
                            tvShowPagingItems = items,
                            config = uiState.config,
                            openTvShowDetails = { tvShow ->
                                analytics.logEvent(
                                    TvAnalyticsEvent.TvShowClicked(
                                        tvShow = tvShow,
                                        section = TvAnalyticsValues.SECTION_LISTING_LIST,
                                        sourceScreen = TvAnalyticsScreenName.TV_LISTING,
                                    )
                                )
                                openTvShowDetails(tvShow.id)
                            },
                            onWatchProviderClick = { provider ->
                                analytics.logEvent(
                                    TvAnalyticsEvent.WatchProviderClicked(
                                        providerInfo = provider,
                                        sourceScreen = TvAnalyticsScreenName.TV_LISTING
                                    )
                                )
                                openWatchHub(provider)
                            },
                            onShowFeedback = onShowFeedback,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }

        if (sheetState.isVisible && uiState.isFilterApplicable) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch { sheetState.hide() }
                },
                sheetState = sheetState,
                dragHandle = null,
                sheetGesturesEnabled = false,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                TvFiltersScreen(
                    initialConfig = uiState.filterConfig,
                    onBackPressed = {
                        coroutineScope.launch { sheetState.hide() }
                    },
                    onFilterApplied = { filterConfig ->
                        coroutineScope.launch { sheetState.hide() }
                        viewModel.onFiltersApplied(filterConfig)
                    }
                )
            }
        }
    }
}
