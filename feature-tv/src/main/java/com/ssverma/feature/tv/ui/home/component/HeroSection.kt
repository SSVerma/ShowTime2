package com.ssverma.feature.tv.ui.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.DefaultCoreErrorIndicator
import com.ssverma.core.ui.StatefulContent
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.component.scrim
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.ui.common.TvShowPreviewUiState
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.AppHeroCarousel
import com.ssverma.shared.ui.component.CarouselDefaults
import com.ssverma.shared.ui.component.HeroItem
import com.ssverma.shared.ui.component.HomePageAppBar
import com.ssverma.shared.ui.component.WatchProviderTrigger
import com.ssverma.shared.ui.component.WatchProviderTriggerVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroSection(
    trendingTvShowsState: TvShowPreviewUiState,
    onSearchClicked: () -> Unit,
    onAccountClicked: () -> Unit,
    onTvShowClicked: (TvShowPreview) -> Unit,
    onWatchProviderClick: (ProviderInfo) -> Unit,
    onRetry: () -> Unit,
    onAdLoaded: (InjectableAd, com.google.android.gms.ads.nativead.NativeAd) -> Unit,
    modifier: Modifier = Modifier,
    maxItemWidth: Dp = CarouselDefaults.HeroMaxItemWidth,
    itemHeight: Dp = CarouselDefaults.HeroItemHeight,
    contentPadding: PaddingValues = PaddingValues(horizontal = MaterialTheme.spacing.large),
    overlayContent: (@Composable (TvShowPreview) -> Unit)? = {
        WatchProviderTrigger(
            mediaId = it.id,
            isMovie = false,
            variant = WatchProviderTriggerVariant.Icon,
            onWatchProviderClick = onWatchProviderClick
        )
    }
) {
    StatefulContent(
        state = trendingTvShowsState,
        modifier = modifier,
        loading = {
            HeroShimmerPlaceholder(
                maxItemWidth = maxItemWidth,
                itemHeight = itemHeight,
                contentPadding = contentPadding,
                onSearchClicked = onSearchClicked,
                onAccountClicked = onAccountClicked
            )
        },
        coreErrorContent = { failure ->
            HeroErrorPlaceholder(
                failure = failure,
                onRetry = onRetry,
                itemHeight = itemHeight,
                onSearchClicked = onSearchClicked,
                onAccountClicked = onAccountClicked
            )
        }
    ) { tvShows ->
        val carouselState = rememberCarouselState { tvShows.size }

        val currentBackdrop by remember(tvShows, carouselState.currentItem) {
            derivedStateOf {
                val currentItem = tvShows.getOrNull(carouselState.currentItem)
                if (currentItem is InjectableContent<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (currentItem as InjectableContent<TvShowPreview>).item.backdropImageUrl
                } else {
                    null
                }
            }
        }

        val scrimColor = MaterialTheme.colorScheme.background

        Box(modifier = modifier.fillMaxWidth()) {
            // Background Backdrop
            currentBackdrop?.let { url ->
                NetworkImage(
                    url = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .scrim(
                            colors = listOf(
                                scrimColor.copy(alpha = 0.4f),
                                scrimColor.copy(alpha = 1f)
                            )
                        )
                )
            }

            // Foreground Content
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                HomePageAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    onSearchIconPressed = onSearchClicked,
                    onAccountIconPressed = onAccountClicked
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                AppHeroCarousel<AdInjectable<TvShowPreview>>(
                    items = tvShows,
                    carouselState = carouselState,
                    maxItemWidth = maxItemWidth,
                    itemHeight = itemHeight,
                    contentPadding = contentPadding,
                ) { injectableItem: AdInjectable<TvShowPreview> ->
                    when (injectableItem) {
                        is InjectableAd -> {
                            com.ssverma.shared.ads.native.ShowTimeNativeAd(
                                modifier = Modifier.fillMaxSize(),
                                ad = injectableItem.ad,
                                onAdLoaded = { ad -> onAdLoaded(injectableItem, ad) },
                                style = injectableItem.style
                            )
                        }

                        is InjectableContent<*> -> {
                            val tvShow = (injectableItem as InjectableContent<TvShowPreview>).item
                            HeroItem(
                                title = tvShow.title,
                                imageUrl = tvShow.posterImageUrl,
                                onClick = { onTvShowClicked(tvShow) },
                                overlayContent = { overlayContent?.invoke(tvShow) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroShimmerPlaceholder(
    maxItemWidth: Dp,
    itemHeight: Dp,
    contentPadding: PaddingValues,
    onSearchClicked: () -> Unit,
    onAccountClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        ShimmerPlaceholder(
            modifier = Modifier.matchParentSize(),
            shape = RectangleShape
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            HomePageAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                onSearchIconPressed = onSearchClicked,
                onAccountIconPressed = onAccountClicked
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
                    .padding(bottom = MaterialTheme.spacing.large),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Small Edge Mask
                ShimmerPlaceholder(
                    modifier = Modifier
                        .height(itemHeight)
                        .width(CarouselDefaults.SmallItemMaskWidth),
                    shape = MaterialTheme.shapes.extraLarge
                )

                // Center Large Item
                val centerModifier = if (maxItemWidth == Dp.Unspecified) {
                    Modifier.weight(1f)
                } else {
                    Modifier.width(maxItemWidth)
                }

                ShimmerPlaceholder(
                    modifier = Modifier
                        .height(itemHeight)
                        .then(centerModifier),
                    shape = MaterialTheme.shapes.extraLarge
                )

                // Right Small Edge Mask
                ShimmerPlaceholder(
                    modifier = Modifier
                        .height(itemHeight)
                        .width(CarouselDefaults.SmallItemMaskWidth),
                    shape = MaterialTheme.shapes.extraLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroErrorPlaceholder(
    failure: Failure.CoreFailure,
    onRetry: () -> Unit,
    itemHeight: Dp,
    onSearchClicked: () -> Unit,
    onAccountClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            HomePageAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                onSearchIconPressed = onSearchClicked,
                onAccountIconPressed = onAccountClicked
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            DefaultCoreErrorIndicator(
                failure = failure,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = itemHeight)
            )
        }
    }
}
