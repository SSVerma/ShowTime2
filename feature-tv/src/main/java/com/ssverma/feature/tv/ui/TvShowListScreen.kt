package com.ssverma.feature.tv.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes
import com.ssverma.feature.tv.navigation.args.TvShowListingType
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.ui.component.media.MediaItem
import com.ssverma.shared.ui.component.media.ScoreBadge
import com.ssverma.shared.ui.component.media.ValueIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TvShowListScreen(
    viewModel: TvShowListListViewModel,
    onBackPressed: () -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit
) {
    val moviePagingItems = viewModel.pagedTvShows.collectAsLazyPagingItems()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = viewModel.filterApplicable,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.85f),
                drawerContainerColor = MaterialTheme.colorScheme.background
            ) {
                if (viewModel.filterApplicable) {
                    TvFiltersScreen(
                        filterGroups = viewModel.filterUiState.filters,
                        onFilterApplied = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                            viewModel.onFiltersApplied(it)
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TvShowListAppBar(
                    drawerState,
                    viewModel,
                    onBackPressed,
                    coroutineScope
                )
            }
        ) { padding ->
            PagedContent(pagingItems = moviePagingItems) {
                TvShowsGrid(
                    tvShowPagingItems = it,
                    type = viewModel.listingType,
                    openMovieDetails = { tvShow ->
                        openTvShowDetails(tvShow)
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TvShowListAppBar(
    drawerState: DrawerState,
    viewModel: TvShowListListViewModel,
    onBackPressed: () -> Unit,
    coroutineScope: CoroutineScope
) {
    val navIcon = if (drawerState.isClosed) {
        Icons.Default.ArrowBack
    } else {
        Icons.Default.Close
    }

    val title = if (drawerState.isClosed) {
        viewModel.title ?: stringResource(id = viewModel.titleRes)
    } else {
        stringResource(id = R.string.filter)
    }

    ShowTimeTopAppBar(
        title = title,
        onBackPressed = {
            if (drawerState.isClosed) {
                onBackPressed()
            } else {
                coroutineScope.launch { drawerState.close() }
            }
        },
        navIcon = navIcon,
        actions = {
            if (viewModel.filterApplicable && drawerState.isClosed) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = null
                    )
                }
            }
        }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TvShowsGrid(
    tvShowPagingItems: LazyPagingItems<TvShow>,
    @TvShowListingType type: Int,
    openMovieDetails: (movieId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    PagedGrid(
        pagingItems = tvShowPagingItems,
        contentPadding = PaddingValues(start = 12.dp, top = 12.dp, bottom = 56.dp),
        modifier = modifier
    ) {
        MediaItem(
            title = it.title,
            posterImageUrl = it.posterImageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp, bottom = 12.dp),
            posterModifier = Modifier.fillMaxWidth(),
            titleMaxLines = 2,
            indicator = { Indicator(type = type, tvShow = it) },
            onClick = { openMovieDetails(it.id) }
        )
    }
}

@Composable
private fun Indicator(type: Int, tvShow: TvShow) {
    when (type) {
        TvShowListingAvailableTypes.Popular -> {
            ValueIndicator(value = tvShow.displayPopularity)
        }

        TvShowListingAvailableTypes.TopRated -> {
            ScoreBadge(score = tvShow.voteAvgPercentage)
        }

        TvShowListingAvailableTypes.Upcoming -> {
            tvShow.displayFirstAirDate?.let { date ->
                ValueIndicator(value = date)
            }
        }

        else -> {
            //Nothing
        }
    }
}
