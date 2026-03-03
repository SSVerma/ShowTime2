package com.ssverma.feature.library.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.ui.UiText
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.ui.component.HomePageAppBar
import com.ssverma.shared.ui.component.media.MediaItem
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

@Composable
private fun LibraryContent(
    modifier: Modifier = Modifier,
    tabs: List<LibraryTab>,
    onMovieClicked: (movieId: Int) -> Unit,
    onTvShowClicked: (tvShowId: Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                text = {
                    Text(text = tab.title.asString(), style = MaterialTheme.typography.titleSmall)
                },
                selected = pagerState.currentPage == index,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
            )
        }
    }

    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
