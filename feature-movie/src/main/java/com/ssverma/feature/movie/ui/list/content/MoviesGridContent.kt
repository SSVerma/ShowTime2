package com.ssverma.feature.movie.ui.list.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.media.MovieGridItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoviesGridContent(
    moviePagingItems: LazyPagingItems<MoviePreview>,
    config: MovieListingConfig,
    openMovieDetails: (movie: MoviePreview) -> Unit,
    onWatchProviderClick: (ProviderInfo) -> Unit,
    modifier: Modifier = Modifier,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null
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
                MediaStatsAction(
                    mediaType = MediaType.Movie,
                    mediaId = movie.id,
                    title = movie.title,
                    posterImageUrl = movie.posterImageUrl,
                    backdropImageUrl = movie.backdropImageUrl,
                    voteAvg = movie.voteAvg,
                    releaseDate = movie.displayReleaseDate.orEmpty(),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    onShowFeedback = onShowFeedback,
                    modifier = Modifier.size(32.dp)
                )
            },
            posterModifier = Modifier.fillMaxWidth()
        )
    }
}
