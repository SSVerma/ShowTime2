package com.ssverma.feature.movie.ui.home.component

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.convertor.asMovieListingConfig
import com.ssverma.feature.movie.ui.common.MoviePreviewUiState
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.DiscoveryCategory
import com.ssverma.shared.ui.component.media.MediaCardRatingBadge
import com.ssverma.shared.ui.component.media.MediaItemDefaults
import com.ssverma.shared.ui.component.media.UniversalMediaCard
import com.ssverma.shared.ui.component.media.asUniversalMediaItem
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
                UniversalMediaCard(
                    item = moviePreview.asUniversalMediaItem(),
                    onClick = { onMovieClicked(moviePreview) },
                    isGridView = true,
                    topStartSlot = {
                        val config = categoryPayload.asMovieListingConfig()
                        val hasIndicator = when (config) {
                            is MovieListingConfig.Filterable.Popular,
                            is MovieListingConfig.Filterable.TopRated,
                            is MovieListingConfig.Filterable.Upcoming,
                            is MovieListingConfig.Filterable.NowInCinemas -> true

                            else -> false
                        }
                        if (hasIndicator) {
                            MovieIndicator(config = config, movie = moviePreview)
                        } else if (moviePreview.voteAvg > 0f) {
                            MediaCardRatingBadge(rating = moviePreview.voteAvg)
                        }
                    },
                    onShowFeedback = onShowFeedback,
                    modifier = Modifier.width(MediaItemDefaults.PosterWidth)
                )
            }
        }
    }
}
