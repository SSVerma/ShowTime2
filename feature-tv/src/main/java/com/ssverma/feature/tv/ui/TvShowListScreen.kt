package com.ssverma.feature.tv.ui

import MediaItem
import ScoreIndicator
import ValueIndicator
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes
import com.ssverma.feature.tv.navigation.args.TvShowListingType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun TvShowListScreen(
    viewModel: TvShowListListViewModel,
    onBackPressed: () -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit
) {
    val moviePagingItems = viewModel.pagedTvShows.collectAsLazyPagingItems()

    val backdropScaffoldState =
        rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
    val coroutineScope = rememberCoroutineScope()

    BackdropScaffold(
        scaffoldState = backdropScaffoldState,
        backLayerBackgroundColor = MaterialTheme.colors.background,
        frontLayerBackgroundColor = MaterialTheme.colors.background,
        appBar = {
            TvShowListAppBar(
                backdropScaffoldState,
                viewModel,
                onBackPressed,
                coroutineScope
            )
        },
        backLayerContent = {
            if (viewModel.filterApplicable) {
                TvFiltersScreen(
                    filterGroups = viewModel.filterUiState.filters,
                    onFilterApplied = {
                        coroutineScope.launch {
                            backdropScaffoldState.conceal()
                        }
                        viewModel.onFiltersApplied(it)
                    }
                )
            } else {
                //Workaround for -> java.lang.IllegalArgumentException: The initial value must have an associated anchor.
                //reason is peekHeight
                Box(modifier = Modifier.height(1.dp))
            }
        },
        frontLayerContent = {
            PagedContent(pagingItems = moviePagingItems) {
                TvShowsGrid(
                    tvShowPagingItems = it,
                    type = viewModel.listingType,
                    openMovieDetails = { tvShow ->
                        openTvShowDetails(tvShow)
                    }
                )
            }
        },
        headerHeight = BackdropScaffoldDefaults.HeaderHeight + BackdropScaffoldDefaults.HeaderHeight,
        modifier = Modifier.statusBarsPadding()
    ) {

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TvShowListAppBar(
    backdropScaffoldState: BackdropScaffoldState,
    viewModel: TvShowListListViewModel,
    onBackPressed: () -> Unit,
    coroutineScope: CoroutineScope
) {
    val navIconRes = if (backdropScaffoldState.isConcealed) {
        AppIcons.ArrowBack
    } else {
        AppIcons.Close
    }

    val title = if (backdropScaffoldState.isConcealed) {
        viewModel.title ?: stringResource(id = viewModel.titleRes)
    } else {
        stringResource(id = R.string.filter)
    }

    ShowTimeTopAppBar(
        title = title,
        backgroundColor = MaterialTheme.colors.background,
        elevation = 0.dp,
        onBackPressed = {
            if (backdropScaffoldState.isConcealed) {
                onBackPressed()
            } else {
                coroutineScope.launch { backdropScaffoldState.conceal() }
            }
        },
        navIcon = navIconRes,
        actions = {
            if (viewModel.filterApplicable && backdropScaffoldState.isConcealed) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (backdropScaffoldState.isConcealed) {
                                backdropScaffoldState.reveal()
                            } else {
                                backdropScaffoldState.conceal()
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
    openMovieDetails: (movieId: Int) -> Unit
) {
    PagedGrid(
        pagingItems = tvShowPagingItems,
        contentPadding = PaddingValues(start = 12.dp, top = 12.dp, bottom = 56.dp)
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
            ScoreIndicator(score = tvShow.voteAvgPercentage)
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