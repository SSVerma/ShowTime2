package com.ssverma.feature.tv.ui.home.component

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
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
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.ui.list.component.TvIndicator
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.MediaItemShimmer
import com.ssverma.shared.ui.component.WatchProviderTrigger
import com.ssverma.shared.ui.component.WatchProviderTriggerVariant
import com.ssverma.shared.ui.component.media.TvShowGridItem
import com.ssverma.feature.tv.ui.common.TvShowPreviewUiState

data class DiscoveryCategory(
    @StringRes val titleRes: Int,
    val config: TvShowListingConfig,
    val uiState: TvShowPreviewUiState
)

@OptIn(ExperimentalMaterial3Api::class)
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
    onAdLoaded: (InjectableAd, com.google.android.gms.ads.nativead.NativeAd) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val categories = remember(popularTvShowsState, topRatedTvShowsState, upcomingTvShowsState) {
        listOf(
            DiscoveryCategory(
                titleRes = R.string.popular,
                config = TvShowListingConfig.Filterable.Popular(),
                uiState = popularTvShowsState
            ),
            DiscoveryCategory(
                titleRes = R.string.top_rated,
                config = TvShowListingConfig.Filterable.TopRated(),
                uiState = topRatedTvShowsState
            ),
            DiscoveryCategory(
                titleRes = R.string.upcoming,
                config = TvShowListingConfig.Filterable.Upcoming(),
                uiState = upcomingTvShowsState
            )
        )
    }

    LaunchedEffect(selectedTabIndex) {
        when (categories[selectedTabIndex].config) {
            is TvShowListingConfig.Filterable.Popular -> onFetchPopular()
            is TvShowListingConfig.Filterable.TopRated -> onFetchTopRated()
            is TvShowListingConfig.Filterable.Upcoming -> onFetchUpcoming()
            else -> {}
        }
    }

    Column(modifier = modifier) {
        SectionHeader(
            modifier = Modifier.padding(start = MaterialTheme.spacing.large),
            title = stringResource(R.string.discover),
            onTrailingActionClicked = {
                val category = categories[selectedTabIndex]
                val args = when (category.config) {
                    is TvShowListingConfig.Filterable.Popular -> TvShowListingArgs.Popular(titleRes = category.titleRes)
                    is TvShowListingConfig.Filterable.TopRated -> TvShowListingArgs.TopRated(titleRes = category.titleRes)
                    is TvShowListingConfig.Filterable.Upcoming -> TvShowListingArgs.Upcoming(titleRes = category.titleRes)
                    else -> TvShowListingArgs.Popular(titleRes = category.titleRes)
                }
                onSeeAllClicked(TvShowListingRoute(args = args))
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
                    when (category.config) {
                        is TvShowListingConfig.Filterable.Popular -> onFetchPopular()
                        is TvShowListingConfig.Filterable.TopRated -> onFetchTopRated()
                        is TvShowListingConfig.Filterable.Upcoming -> onFetchUpcoming()
                        else -> {}
                    }
                }
            ) { tvShows ->
                HorizontalLazyList(
                    items = tvShows,
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) { injectableItem ->
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
                            val tvShowPreview = (element as InjectableContent<TvShowPreview>).item
                            TvShowGridItem(
                                tvShow = tvShowPreview,
                                showRating = category.config !is TvShowListingConfig.Filterable.Upcoming && category.config !is TvShowListingConfig.Filterable.TopRated,
                                indicator = { preview ->
                                    TvIndicator(config = category.config, tvShow = preview)
                                },
                                onClick = { preview -> onTvShowClicked(preview) },
                                overlayContent = {
                                    WatchProviderTrigger(
                                        mediaId = tvShowPreview.id,
                                        isMovie = false,
                                        variant = WatchProviderTriggerVariant.Icon,
                                        modifier = Modifier.padding(MaterialTheme.spacing.small),
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
