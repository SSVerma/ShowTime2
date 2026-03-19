package com.ssverma.feature.movie.ui.list.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.WatchProviderTrigger
import com.ssverma.shared.ui.component.WatchProviderTriggerVariant
import com.ssverma.shared.ui.component.media.MovieGridItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoviesGridContent(
    moviePagingItems: LazyPagingItems<MoviePreview>,
    config: MovieListingConfig,
    openMovieDetails: (movie: MoviePreview) -> Unit,
    onWatchProviderClick: (ProviderInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    PagedGrid(
        pagingItems = moviePagingItems,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
    ) { movie ->
        val showRating = config !is MovieListingConfig.Filterable.Upcoming &&
                config !is MovieListingConfig.Filterable.TopRated

        MovieGridItem(
            movie = movie,
            showRating = showRating,
            indicator = { preview -> MovieIndicator(config = config, movie = preview) },
            onClick = { preview -> openMovieDetails(preview) },
            overlayContent = {
                WatchProviderTrigger(
                    mediaId = it.id,
                    isMovie = true,
                    variant = WatchProviderTriggerVariant.Icon,
                    onWatchProviderClick = onWatchProviderClick,
                    modifier = Modifier.padding(MaterialTheme.spacing.small)
                )
            },
            posterModifier = Modifier.fillMaxWidth()
        )
    }
}
