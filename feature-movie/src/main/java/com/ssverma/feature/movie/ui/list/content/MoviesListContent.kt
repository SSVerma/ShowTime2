package com.ssverma.feature.movie.ui.list.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.ui.paging.PagedList
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.media.MediaCardRatingBadge
import com.ssverma.shared.ui.component.media.UniversalMediaCard
import com.ssverma.shared.ui.component.media.asUniversalMediaItem

@Composable
fun MoviesListContent(
    moviePagingItems: LazyPagingItems<MoviePreview>,
    config: MovieListingConfig,
    openMovieDetails: (movie: MoviePreview) -> Unit,
    onWatchProviderClick: (ProviderInfo) -> Unit,
    modifier: Modifier = Modifier,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null
) {
    PagedList(
        pagingItems = moviePagingItems,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
    ) { movie ->
        UniversalMediaCard(
            item = movie.asUniversalMediaItem(),
            onClick = { openMovieDetails(movie) },
            isGridView = false,
            topStartSlot = {
                val hasIndicator = when (config) {
                    is MovieListingConfig.Filterable.Popular,
                    is MovieListingConfig.Filterable.TopRated,
                    is MovieListingConfig.Filterable.Upcoming,
                    is MovieListingConfig.Filterable.NowInCinemas -> true

                    else -> false
                }
                if (hasIndicator) {
                    MovieIndicator(config = config, movie = movie)
                } else if (movie.voteAvg > 0f) {
                    MediaCardRatingBadge(rating = movie.voteAvg)
                }
            },
            onShowFeedback = onShowFeedback,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
