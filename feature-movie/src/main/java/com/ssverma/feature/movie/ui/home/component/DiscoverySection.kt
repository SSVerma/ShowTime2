package com.ssverma.feature.movie.ui.home.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.convertor.asMovieListingConfig
import com.ssverma.feature.movie.ui.common.MoviePreviewUiState
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.DiscoveryCategory
import com.ssverma.shared.ui.component.media.MovieGridItem
import com.ssverma.shared.ui.component.DiscoverySection as SharedDiscoverySection

@Composable
fun DiscoverySection(
    popularMoviesState: MoviePreviewUiState,
    topRatedMoviesState: MoviePreviewUiState,
    upcomingMoviesState: MoviePreviewUiState,
    onFetchPopular: () -> Unit,
    onFetchTopRated: () -> Unit,
    onFetchUpcoming: () -> Unit,
    onMovieClicked: (movie: MoviePreview) -> Unit,
    onSeeAllClicked: (MovieListingArgs) -> Unit,
    onWatchProviderClick: (provider: ProviderInfo) -> Unit,
    onAdLoaded: (InjectableAd, NativeAd) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = false,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null
) {
    val categories = remember(popularMoviesState, topRatedMoviesState, upcomingMoviesState) {
        listOf(
            DiscoveryCategory(
                titleRes = R.string.popuplar,
                payload = MovieListingArgs.Popular(titleRes = R.string.popuplar),
                uiState = popularMoviesState,
                onFetchData = onFetchPopular
            ),
            DiscoveryCategory(
                titleRes = R.string.top_rated,
                payload = MovieListingArgs.TopRated(titleRes = R.string.top_rated),
                uiState = topRatedMoviesState,
                onFetchData = onFetchTopRated
            ),
            DiscoveryCategory(
                titleRes = R.string.upcoming,
                payload = MovieListingArgs.Upcoming(titleRes = R.string.upcoming),
                uiState = upcomingMoviesState,
                onFetchData = onFetchUpcoming
            )
        )
    }

    SharedDiscoverySection(
        categories = categories,
        onSeeAllClicked = onSeeAllClicked,
        modifier = modifier,
        showHeader = showHeader
    ) { categoryPayload, injectableItem ->
        when (injectableItem) {
            is InjectableAd -> {
                ShowTimeNativeAd(
                    ad = injectableItem.ad,
                    onAdLoaded = { ad -> onAdLoaded(injectableItem, ad) },
                    style = injectableItem.style
                )
            }

            is InjectableContent<*> -> {
                @Suppress("UNCHECKED_CAST")
                val moviePreview = (injectableItem as InjectableContent<MoviePreview>).item
                MovieGridItem(
                    movie = moviePreview,
                    onClick = onMovieClicked,
                    indicator = {
                        MovieIndicator(
                            config = categoryPayload.asMovieListingConfig(),
                            movie = it
                        )
                    },
                    overlayContent = {
                        MediaStatsAction(
                            mediaType = MediaType.Movie,
                            mediaId = moviePreview.id,
                            title = moviePreview.title,
                            posterImageUrl = moviePreview.posterImageUrl,
                            backdropImageUrl = moviePreview.backdropImageUrl,
                            voteAvg = moviePreview.voteAvg,
                            releaseDate = moviePreview.displayReleaseDate.orEmpty(),
                            containerColor = MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.85f
                            ),
                            onShowFeedback = onShowFeedback,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                )
            }
        }
    }
}
