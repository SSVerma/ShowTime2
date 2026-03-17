package com.ssverma.feature.movie.ui.list.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.ui.paging.PagedList
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.navigation.args.MovieListingAvailableTypes
import com.ssverma.feature.movie.navigation.args.MovieListingType
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.media.MovieListItem

@Composable
fun MoviesListContent(
    moviePagingItems: LazyPagingItems<MoviePreview>,
    @MovieListingType type: Int,
    openMovieDetails: (movie: MoviePreview) -> Unit,
    onWatchProviderClick: (ProviderInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    PagedList(
        pagingItems = moviePagingItems,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
    ) { movie ->
        MovieListItem(
            movie = movie,
            showRating = type != MovieListingAvailableTypes.Upcoming && type != MovieListingAvailableTypes.TopRated,
            indicator = { preview -> MovieIndicator(type = type, movie = preview) },
            onWatchProviderClick = onWatchProviderClick,
            onClick = { preview -> openMovieDetails(preview) },
        )
    }
}
