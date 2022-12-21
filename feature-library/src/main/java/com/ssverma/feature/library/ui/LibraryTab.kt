package com.ssverma.feature.library.ui

import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.ui.UiText
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.tv.TvShow

data class LibraryTab(
    val title: UiText,
    val tabType: LibraryTabType
)

sealed interface LibraryTabType {
    data class FavoriteMovies(
        val movies: LazyPagingItems<Movie>
    ) : LibraryTabType

    data class FavoriteTvShows(
        val tvShows: LazyPagingItems<TvShow>
    ) : LibraryTabType

    data class WatchlistMovies(
        val movies: LazyPagingItems<Movie>
    ) : LibraryTabType

    data class WatchlistTvShows(
        val tvShows: LazyPagingItems<TvShow>
    ) : LibraryTabType
}