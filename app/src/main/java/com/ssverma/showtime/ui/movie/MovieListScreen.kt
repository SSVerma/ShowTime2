package com.ssverma.showtime.ui.movie

import MovieItem
import ScoreIndicator
import ValueIndicator
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.ui.common.PagedContent
import com.ssverma.showtime.ui.common.PagedGrid
import com.ssverma.showtime.ui.common.Screen

object MovieListingType {
    const val Trending = "trending"
    const val Popular = "popular"
    const val TopRated = "top-rated"
    const val NowInCinemas = "now-in-cinemas"
    const val Upcoming = "upcoming"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieListScreen(
    title: String,
    type: String,
    viewModel: MovieListViewModel,
    onBackPressed: () -> Unit
) {
    LaunchedEffect(key1 = type) {
        viewModel.onMovieTypeAvailable(type = type)//TODO: find better way
    }

    Screen(
        title = title,
        onBackPressed = onBackPressed
    ) {
        val moviePagingItems = viewModel.pagedMovies.collectAsLazyPagingItems()

        PagedContent(pagingItems = moviePagingItems) {
            MoviesGrid(moviePagingItems = it, type)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoviesGrid(moviePagingItems: LazyPagingItems<Movie>, type: String) {
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
private fun Indicator(type: String, movie: Movie) {
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
    }
}