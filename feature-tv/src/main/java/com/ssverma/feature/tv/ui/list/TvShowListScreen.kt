package com.ssverma.feature.tv.ui.list

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.feature.tv.ui.filter.TvFiltersScreen
import com.ssverma.feature.tv.ui.list.component.TvShowListTopBar
import com.ssverma.feature.tv.ui.list.content.TvShowsGridContent
import com.ssverma.feature.tv.ui.list.content.TvShowsListContent
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.core.ui.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TvShowListScreen(
    onBackPressed: () -> Unit,
    openTvShowDetails: (Int) -> Unit,
    viewModel: TvShowListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tvShowPagingItems = viewModel.pagedTvShows.collectAsLazyPagingItems()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    AppPage(
        scrollBehavior = scrollBehavior,
        topBar = { behavior ->
            TvShowListTopBar(
                uiState = uiState,
                onToggleViewMode = { viewModel.toggleViewMode() },
                onOpenFilters = { showFilterSheet = true },
                onBackPressed = onBackPressed,
                scrollBehavior = behavior
            )
        }
    ) { innerPadding ->

        PagedContent(pagingItems = tvShowPagingItems) { items ->
            Crossfade(uiState.isGridView, label = "TvShowListViewModeTransition") { isGrid ->
                if (isGrid) {
                    TvShowsGridContent(
                        tvShowPagingItems = items,
                        type = uiState.listingType,
                        openTvShowDetails = openTvShowDetails,
                        modifier = Modifier.padding(innerPadding),
                    )
                } else {
                    TvShowsListContent(
                        tvShowPagingItems = items,
                        type = uiState.listingType,
                        openTvShowDetails = openTvShowDetails,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    if (showFilterSheet && uiState.isFilterApplicable) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
        ) {
            TvFiltersScreen(
                filterGroups = uiState.filterUiState.filters,
                onFilterApplied = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showFilterSheet = false
                        }
                    }
                    viewModel.onFiltersApplied(it)
                }
            )
        }
    }

}
