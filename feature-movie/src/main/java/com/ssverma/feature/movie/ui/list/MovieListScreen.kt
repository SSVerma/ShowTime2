package com.ssverma.feature.movie.ui.list

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
    val watchRegion by viewModel.appConfigRepository.watchProviderRegion.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    AppPage(
        scrollBehavior = scrollBehavior,
        topBar = { behavior ->
            MovieListTopBar(
                uiState = uiState,
                onToggleViewMode = { viewModel.toggleViewMode() },
                onOpenFilters = {
                    coroutineScope.launch { sheetState.show() }
                },
                onBackPressed = onBackPressed,
                scrollBehavior = behavior
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
            MovieFiltersScreen(
                watchRegion = watchRegion,
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
