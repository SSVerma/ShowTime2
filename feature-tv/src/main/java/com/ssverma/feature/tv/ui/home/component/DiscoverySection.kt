package com.ssverma.feature.tv.ui.home.component

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.ui.common.TvShowPreviewUiState
import com.ssverma.feature.tv.ui.list.component.TvIndicator
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.DiscoveryCategory
import com.ssverma.shared.ui.component.media.MediaCardRatingBadge
import com.ssverma.shared.ui.component.media.MediaItemDefaults
import com.ssverma.shared.ui.component.media.UniversalMediaCard
import com.ssverma.shared.ui.component.media.asUniversalMediaItem
import com.ssverma.shared.ui.component.DiscoverySection as SharedDiscoverySection

@Composable
fun DiscoverySection(
    popularTvShowsState: TvShowPreviewUiState,
    topRatedTvShowsState: TvShowPreviewUiState,
    upcomingTvShowsState: TvShowPreviewUiState,
    onFetchPopular: () -> Unit,
    onFetchTopRated: () -> Unit,
    onFetchUpcoming: () -> Unit,
    onTvShowClicked: (tvShow: TvShowPreview) -> Unit,
    onSeeAllClicked: (TvShowListingRoute) -> Unit,
    onWatchProviderClick: (provider: ProviderInfo) -> Unit,
    onAdLoaded: (InjectableAd, NativeAd) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = false,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null
) {
    val categories = remember(popularTvShowsState, topRatedTvShowsState, upcomingTvShowsState) {
        listOf(
            DiscoveryCategory(
                titleRes = R.string.popular,
                payload = TvShowListingConfig.Filterable.Popular(),
                uiState = popularTvShowsState,
                onFetchData = onFetchPopular
            ),
            DiscoveryCategory(
                titleRes = R.string.top_rated,
                payload = TvShowListingConfig.Filterable.TopRated(),
                uiState = topRatedTvShowsState,
                onFetchData = onFetchTopRated
            ),
            DiscoveryCategory(
                titleRes = R.string.upcoming,
                payload = TvShowListingConfig.Filterable.Upcoming(),
                uiState = upcomingTvShowsState,
                onFetchData = onFetchUpcoming
            )
        )
    }

    SharedDiscoverySection(
        categories = categories,
        onSeeAllClicked = { config ->
            val args = when (config) {
                is TvShowListingConfig.Filterable.Popular -> TvShowListingArgs.Popular(
                    titleRes = R.string.popular
                )

                is TvShowListingConfig.Filterable.TopRated -> TvShowListingArgs.TopRated(
                    titleRes = R.string.top_rated
                )

                is TvShowListingConfig.Filterable.Upcoming -> TvShowListingArgs.Upcoming(
                    titleRes = R.string.upcoming
                )

                else -> TvShowListingArgs.Popular(titleRes = R.string.popular)
            }
            onSeeAllClicked(TvShowListingRoute(args = args))
        },
        modifier = modifier,
        showHeader = showHeader
    ) { config, injectableItem ->
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
                val tvShowPreview = (injectableItem as InjectableContent<TvShowPreview>).item
                UniversalMediaCard(
                    item = tvShowPreview.asUniversalMediaItem(),
                    onClick = { onTvShowClicked(tvShowPreview) },
                    isGridView = true,
                    topStartSlot = {
                        val hasIndicator = when (config) {
                            is TvShowListingConfig.Filterable.Popular,
                            is TvShowListingConfig.Filterable.TopRated,
                            is TvShowListingConfig.Filterable.Upcoming -> true

                            else -> false
                        }
                        if (hasIndicator) {
                            TvIndicator(config = config, tvShow = tvShowPreview)
                        } else if (tvShowPreview.voteAvg > 0f) {
                            MediaCardRatingBadge(rating = tvShowPreview.voteAvg)
                        }
                    },
                    onShowFeedback = onShowFeedback,
                    modifier = Modifier.width(MediaItemDefaults.PosterWidth)
                )
            }
        }
    }
}
