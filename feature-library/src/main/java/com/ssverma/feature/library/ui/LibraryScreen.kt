package com.ssverma.feature.library.ui

import MediaItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.ssverma.core.ui.UiText
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.ui.component.HomePageAppBar
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    viewModel: LibraryHomeViewModel = hiltViewModel(),
    onMovieClicked: (movieId: Int) -> Unit,
    onTvShowClicked: (tvShowId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit
) {
    // TODO: Revisit - Don't hit all tab's api call at once
    val favoriteMovies = viewModel.pagedFavoriteMovies.collectAsLazyPagingItems()
    val favoriteTvShows = viewModel.pagedFavoriteTvShows.collectAsLazyPagingItems()
    val watchlistMovies = viewModel.pagedWatchlistMovies.collectAsLazyPagingItems()
    val watchlistTvShows = viewModel.pagedWatchlistTvShows.collectAsLazyPagingItems()

    val tabs = remember {
        listOf(
            LibraryTab(
                title = UiText.StaticText(resId = R.string.favorite_movies),
                tabType = LibraryTabType.FavoriteMovies(
                    movies = favoriteMovies
                )
            ),
            LibraryTab(
                title = UiText.StaticText(resId = R.string.favorite_tv_shows),
                tabType = LibraryTabType.FavoriteTvShows(
                    tvShows = favoriteTvShows
                )
            ),
            LibraryTab(
                title = UiText.StaticText(resId = R.string.watchlist_movies),
                tabType = LibraryTabType.WatchlistMovies(
                    movies = watchlistMovies
                )
            ),
            LibraryTab(
                title = UiText.StaticText(resId = R.string.watchlist_tv_shows),
                tabType = LibraryTabType.WatchlistTvShows(
                    tvShows = watchlistTvShows
                )
            )
        )
    }

    Column {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        HomePageAppBar(
            backgroundColor = MaterialTheme.colors.background,
            onSearchIconPressed = openSearchPage,
            onAccountIconPressed = openAccountPage
        )
        LibraryContent(
            tabs = tabs,
            onMovieClicked = onMovieClicked,
            onTvShowClicked = onTvShowClicked
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun LibraryContent(
    modifier: Modifier = Modifier,
    tabs: List<LibraryTab>,
    onMovieClicked: (movieId: Int) -> Unit,
    onTvShowClicked: (tvShowId: Int) -> Unit
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        },
        backgroundColor = MaterialTheme.colors.background,
        modifier = modifier
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                text = {
                    Text(text = tab.title.asString())
                },
                selected = pagerState.currentPage == index,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
            )
        }
    }

    HorizontalPager(
        state = pagerState,
        count = tabs.size,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
    ) { page ->
        val tab = tabs[page]

        when (tab.tabType) {
            is LibraryTabType.FavoriteMovies -> {
                PagedContent(pagingItems = tab.tabType.movies) {
                    MoviesGrid(
                        moviePagingItems = it,
                        onMovieClicked = onMovieClicked
                    )
                }
            }
            is LibraryTabType.FavoriteTvShows -> {
                PagedContent(pagingItems = tab.tabType.tvShows) {
                    TvShowsGrid(
                        tvShowPagingItems = it,
                        onTvShowClicked = onTvShowClicked
                    )
                }
            }
            is LibraryTabType.WatchlistMovies -> {
                PagedContent(pagingItems = tab.tabType.movies) {
                    MoviesGrid(
                        moviePagingItems = it,
                        onMovieClicked = onMovieClicked
                    )
                }
            }
            is LibraryTabType.WatchlistTvShows -> {
                PagedContent(pagingItems = tab.tabType.tvShows) {
                    TvShowsGrid(
                        tvShowPagingItems = it,
                        onTvShowClicked = onTvShowClicked
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TvShowsGrid(
    tvShowPagingItems: LazyPagingItems<TvShow>,
    onTvShowClicked: (movieId: Int) -> Unit
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
            indicator = null,
            onClick = { onTvShowClicked(it.id) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoviesGrid(
    moviePagingItems: LazyPagingItems<Movie>,
    onMovieClicked: (movieId: Int) -> Unit
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
            indicator = null,
            onClick = { onMovieClicked(it.id) }
        )
    }
}