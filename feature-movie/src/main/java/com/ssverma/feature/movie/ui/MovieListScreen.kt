package com.ssverma.feature.movie.ui

import MediaItem
import ScoreIndicator
import ValueIndicator
import androidx.activity.compose.BackHandler
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
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.feature.movie.navigation.args.MovieListingAvailableTypes
import com.ssverma.feature.movie.navigation.args.MovieListingType
import com.ssverma.feature.movie.ui.filter.MovieFiltersScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MovieListScreen(
    viewModel: MovieListViewModel,
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit
) {
    val moviePagingItems = viewModel.pagedMovies.collectAsLazyPagingItems()

    val backdropScaffoldState =
        rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
    val coroutineScope = rememberCoroutineScope()

    BackdropScaffold(
        scaffoldState = backdropScaffoldState,
        backLayerBackgroundColor = MaterialTheme.colors.background,
        frontLayerBackgroundColor = MaterialTheme.colors.background,
        gesturesEnabled = viewModel.filterApplicable,
        appBar = {
            MovieListAppBar(
                backdropScaffoldState,
                viewModel,
                onBackPressed,
                coroutineScope
            )
        },
        backLayerContent = {
            if (backdropScaffoldState.isRevealed) {
                BackHandler {
                    coroutineScope.launch {
                        backdropScaffoldState.conceal()
                    }
                }
            }
            if (viewModel.filterApplicable) {
                MovieFiltersScreen(
                    filterGroups = viewModel.filters.filters,
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
                MoviesGrid(
                    moviePagingItems = it,
                    type = viewModel.listingType,
                    openMovieDetails = { movie ->
                        openMovieDetails(movie)
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
private fun MovieListAppBar(
    backdropScaffoldState: BackdropScaffoldState,
    viewModel: MovieListViewModel,
    onBackPressed: () -> Unit,
    coroutineScope: CoroutineScope
) {
    val navIcon = if (backdropScaffoldState.isConcealed) {
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
        navIcon = navIcon,
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
fun MoviesGrid(
    moviePagingItems: LazyPagingItems<Movie>,
    @MovieListingType
    type: Int,
    openMovieDetails: (movieId: Int) -> Unit
) {
    PagedGrid(
        pagingItems = moviePagingItems,
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
            indicator = { Indicator(type = type, movie = it) },
            onClick = { openMovieDetails(it.id) }
        )
    }
}

@Composable
private fun Indicator(@MovieListingType type: Int, movie: Movie) {
    when (type) {
        MovieListingAvailableTypes.Popular -> {
            ValueIndicator(value = movie.displayPopularity)
        }
        MovieListingAvailableTypes.TopRated -> {
            ScoreIndicator(score = movie.voteAvgPercentage)
        }
        MovieListingAvailableTypes.Upcoming -> {
            movie.displayReleaseDate?.let { date ->
                ValueIndicator(value = date)
            }
        }
        else -> {
            //Nothing
        }
    }
}