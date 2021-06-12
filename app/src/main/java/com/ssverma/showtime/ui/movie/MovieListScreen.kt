package com.ssverma.showtime.ui.movie

import MovieItem
import ScoreIndicator
import ValueIndicator
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.insets.statusBarsPadding
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.ui.FiltersScreen
import com.ssverma.showtime.ui.common.AppTopAppBar
import com.ssverma.showtime.ui.common.PagedContent
import com.ssverma.showtime.ui.common.PagedGrid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class MovieListingType {
    TrendingToday,
    Popular,
    TopRated,
    NowInCinemas,
    Upcoming,
    Genre
}

data class MovieListLaunchable(
    val listingType: MovieListingType,
    @StringRes val titleRes: Int = 0,
    val title: String? = null,
    val genre: Genre? = null
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MovieListScreen(
    viewModel: MovieListViewModel,
    onBackPressed: () -> Unit
) {
    val moviePagingItems = viewModel.pagedMovies.collectAsLazyPagingItems()

    val backdropScaffoldState =
        rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
    val coroutineScope = rememberCoroutineScope()

    BackdropScaffold(
        scaffoldState = backdropScaffoldState,
        backLayerBackgroundColor = MaterialTheme.colors.background,
        frontLayerBackgroundColor = MaterialTheme.colors.background,
        appBar = {
            MovieListAppBar(
                backdropScaffoldState,
                viewModel,
                onBackPressed,
                coroutineScope
            )
        },
        backLayerContent = {
            if (viewModel.filterApplicable) {
                val filterGroups by viewModel.filters.collectAsState(initial = emptyList())
                FiltersScreen(
                    filterGroups = filterGroups,
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
                MoviesGrid(moviePagingItems = it, viewModel.listingType)
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
    val navIconRes = if (backdropScaffoldState.isConcealed) {
        R.drawable.ic_arrow_back
    } else {
        R.drawable.ic_close
    }

    val title = if (backdropScaffoldState.isConcealed) {
        viewModel.title ?: stringResource(id = viewModel.titleRes)
    } else {
        stringResource(id = R.string.filter)
    }

    AppTopAppBar(
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
        navIconRes = navIconRes,
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
fun MoviesGrid(moviePagingItems: LazyPagingItems<Movie>, type: MovieListingType) {
    PagedGrid(
        pagingItems = moviePagingItems,
        contentPadding = PaddingValues(start = 12.dp, top = 12.dp, bottom = 56.dp)
    ) {
        MovieItem(
            title = it.title,
            posterImageUrl = it.posterImageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp, bottom = 12.dp),
            titleMaxLines = 2,
            indicator = { Indicator(type = type, movie = it) }
        )
    }
}

@Composable
private fun Indicator(type: MovieListingType, movie: Movie) {
    when (type) {
        MovieListingType.Popular -> {
            ValueIndicator(value = movie.displayPopularity)
        }
        MovieListingType.TopRated -> {
            ScoreIndicator(score = movie.voteAvgPercentage)
        }
        MovieListingType.Upcoming -> {
            movie.displayReleaseDate?.let { date ->
                ValueIndicator(value = date)
            }
        }
        else -> {
            //Nothing
        }
    }
}