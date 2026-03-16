package com.ssverma.feature.tv.ui.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.component.scrim
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.CarouselDefaults
import com.ssverma.shared.ui.component.HomePageAppBar
import com.ssverma.shared.ui.component.WatchProviderTrigger
import com.ssverma.shared.ui.component.WatchProviderTriggerVariant
import com.ssverma.shared.ui.component.AppHeroCarousel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroSection(
    trendingTvShowsState: UiState<List<TvShowPreview>, TvShowFailure>,
    onSearchClicked: () -> Unit,
    onAccountClicked: () -> Unit,
    onTvShowClicked: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    maxItemWidth: Dp = CarouselDefaults.HeroMaxItemWidth,
    itemHeight: Dp = CarouselDefaults.HeroItemHeight,
    contentPadding: PaddingValues = PaddingValues(horizontal = MaterialTheme.spacing.large),
    overlayContent: (@Composable (TvShowPreview) -> Unit)? = {
        WatchProviderTrigger(
            mediaId = it.id,
            isMovie = false,
            variant = WatchProviderTriggerVariant.Icon
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

        val currentBackdrop by remember {
            derivedStateOf { tvShows.getOrNull(carouselState.currentItem)?.backdropImageUrl }
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

                AppHeroCarousel(
                    items = tvShows,
                    carouselState = carouselState,
                    maxItemWidth = maxItemWidth,
                    itemHeight = itemHeight,
                    contentPadding = contentPadding,
                    imageUrl = { it.posterImageUrl },
                    title = { it.title },
                    onItemClick = { onTvShowClicked(it.id) },
                    overlayContent = overlayContent
                )
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
