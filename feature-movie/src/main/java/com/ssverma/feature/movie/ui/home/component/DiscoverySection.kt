package com.ssverma.feature.movie.ui.home.component

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.convertor.asMovieListingConfig
import com.ssverma.feature.movie.ui.common.MoviePreviewUiState
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.MediaItemShimmer
import com.ssverma.shared.ui.component.WatchProviderTrigger
import com.ssverma.shared.ui.component.WatchProviderTriggerVariant
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.ui.NativeAdStyle

import com.ssverma.shared.ui.component.media.MovieGridItem

data class DiscoveryCategory(
    @param:StringRes val titleRes: Int,
    val route: MovieListingArgs,
    val uiState: MoviePreviewUiState
)

@OptIn(ExperimentalMaterial3Api::class)
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
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val categories = remember(popularMoviesState, topRatedMoviesState, upcomingMoviesState) {
        listOf(
            DiscoveryCategory(
                titleRes = R.string.popuplar,
                route = MovieListingArgs.Popular(titleRes = R.string.popuplar),
                uiState = popularMoviesState
            ),
            DiscoveryCategory(
                titleRes = R.string.top_rated,
                route = MovieListingArgs.TopRated(titleRes = R.string.top_rated),
                uiState = topRatedMoviesState
            ),
            DiscoveryCategory(
                titleRes = R.string.upcoming,
                route = MovieListingArgs.Upcoming(titleRes = R.string.upcoming),
                uiState = upcomingMoviesState
            )
        )
    }

    LaunchedEffect(selectedTabIndex) {
        when (categories[selectedTabIndex].route) {
            is MovieListingArgs.Popular -> onFetchPopular()
            is MovieListingArgs.TopRated -> onFetchTopRated()
            is MovieListingArgs.Upcoming -> onFetchUpcoming()
            else -> {}
        }
    }

    Column(modifier = modifier) {
        SectionHeader(
            modifier = Modifier.padding(start = MaterialTheme.spacing.large),
            title = stringResource(R.string.discover),
            onTrailingActionClicked = {
                val category = categories[selectedTabIndex]
                onSeeAllClicked(category.route)
            }
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.smallMedium)
        ) {
            itemsIndexed(categories) { index, category ->
                FilterChip(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    label = {
                        Text(
                            text = stringResource(category.titleRes),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }

        AnimatedContent(
            targetState = selectedTabIndex,
            label = "DiscoveryContentTransition"
        ) { index ->
            val category = categories[index]

            DriveCompose(
                uiState = category.uiState,
                loading = { DiscoveryLoadingPlaceholder() },
                onRetry = {
                    when (category.route) {
                        is MovieListingArgs.Popular -> onFetchPopular()
                        is MovieListingArgs.TopRated -> onFetchTopRated()
                        is MovieListingArgs.Upcoming -> onFetchUpcoming()
                        else -> {}
                    }
                }
            ) { movies ->
                LazyRow(
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    items(movies) { injectableItem ->
                        when (val element = injectableItem) {
                            is InjectableAd -> {
                                ShowTimeNativeAd(
                                    ad = element.ad,
                                    onAdLoaded = { ad -> onAdLoaded(element, ad) },
                                    style = element.style
                                )
                            }

                            is InjectableContent<*> -> {
                                @Suppress("UNCHECKED_CAST")
                                val moviePreview = (element as InjectableContent<MoviePreview>).item
                                MovieGridItem(
                                    movie = moviePreview,
                                    onClick = onMovieClicked,
                                    indicator = {
                                        MovieIndicator(
                                            config = category.route.asMovieListingConfig(),
                                            movie = it
                                        )
                                    },
                                    overlayContent = {
                                        WatchProviderTrigger(
                                            mediaId = moviePreview.id,
                                            isMovie = true,
                                            variant = WatchProviderTriggerVariant.Icon,
                                            onWatchProviderClick = onWatchProviderClick,
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryLoadingPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        repeat(3) { MediaItemShimmer() }
    }
}
