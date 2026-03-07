package com.ssverma.feature.movie.ui.list

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
import com.ssverma.feature.movie.ui.filter.MovieFiltersScreen
import com.ssverma.feature.movie.ui.list.component.MovieListTopBar
import com.ssverma.feature.movie.ui.list.content.MoviesGridContent
import com.ssverma.feature.movie.ui.list.content.MoviesListContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    viewModel: MovieListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val moviePagingItems = viewModel.pagedMovies.collectAsLazyPagingItems()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    AppPage(
        scrollBehavior = scrollBehavior,
        topBar = { scrollBehavior ->
            MovieListTopBar(
                uiState = uiState,
                onToggleViewMode = { viewModel.toggleViewMode() },
                onOpenFilters = { showFilterSheet = true },
                onBackPressed = onBackPressed,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->

        PagedContent(pagingItems = moviePagingItems) { items ->
            Crossfade(uiState.isGridView, label = "MovieListViewModeTransition") { isGrid ->
                if (isGrid) {
                    MoviesGridContent(
                        moviePagingItems = items,
                        type = uiState.listingType,
                        openMovieDetails = openMovieDetails,
                        modifier = Modifier.padding(innerPadding),
                    )
                } else {
                    MoviesListContent(
                        moviePagingItems = items,
                        type = uiState.listingType,
                        openMovieDetails = openMovieDetails,
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
            MovieFiltersScreen(
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
