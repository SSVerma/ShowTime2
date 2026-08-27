package com.ssverma.feature.movie.ui.home.component

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.DefaultCoreErrorIndicator
import com.ssverma.core.ui.StatefulContent
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.component.scrim
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.ui.common.MoviePreviewUiState
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.AppHeroCarousel
import com.ssverma.shared.ui.component.CarouselDefaults
import com.ssverma.shared.ui.component.HeroItem
import com.ssverma.shared.ui.component.HomePageAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroSection(
    trendingMoviesState: MoviePreviewUiState,
    onSearchClicked: () -> Unit,
    onAccountClicked: () -> Unit,
    openLibraryPage: (LibraryHomeNavKey) -> Unit,
    onMovieClicked: (movie: MoviePreview) -> Unit,
    onWatchProviderClick: (provider: ProviderInfo) -> Unit,
    onRetry: () -> Unit,
    onAdLoaded: (InjectableAd, NativeAd) -> Unit,
    modifier: Modifier = Modifier,
    carouselState: androidx.compose.material3.carousel.CarouselState? = null,
    showBackdrop: Boolean = false,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null,
    showAppBar: Boolean = false,
    maxItemWidth: Dp = CarouselDefaults.HeroMaxItemWidth,
    itemHeight: Dp = CarouselDefaults.HeroItemHeight,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    overlayContent: (@Composable (MoviePreview) -> Unit)? = { movie ->
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
            modifier = Modifier.size(36.dp)
        )
    }
) {
    StatefulContent(
        state = trendingMoviesState,
        modifier = modifier,
        loading = {
            HeroShimmerPlaceholder(
                showAppBar = showAppBar,
                maxItemWidth = maxItemWidth,
                itemHeight = itemHeight,
                contentPadding = contentPadding,
                onSearchClicked = onSearchClicked,
                onAccountClicked = onAccountClicked
            )
        },
        coreErrorContent = { failure ->
            HeroErrorPlaceholder(
                showAppBar = showAppBar,
                failure = failure,
                onRetry = onRetry,
                itemHeight = itemHeight,
                onSearchClicked = onSearchClicked,
                onAccountClicked = onAccountClicked
            )
        }
    ) { movies ->
        val internalCarouselState = rememberCarouselState { movies.size }
        val effectiveCarouselState = carouselState ?: internalCarouselState

        val currentBackdrop = remember(movies, effectiveCarouselState.currentItem) {
            val currentItem = movies.getOrNull(effectiveCarouselState.currentItem)
            if (currentItem is InjectableContent<*>) {
                (currentItem as InjectableContent<MoviePreview>).item.backdropImageUrl
            } else {
                null
            }
        }

        val scrimColor = MaterialTheme.colorScheme.background

        Box(modifier = modifier.fillMaxWidth()) {
            // Background Backdrop (if enabled locally)
            if (showBackdrop) {
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
            }

            // Foreground Content
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showAppBar) {
                    HomePageAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        onSearchIconPressed = onSearchClicked,
                        onAccountIconPressed = onAccountClicked,
                        onLibraryIconPressed = { openLibraryPage(LibraryHomeNavKey.Default) }
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                } else {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                }

                AppHeroCarousel(
                    items = movies,
                    carouselState = effectiveCarouselState,
                    maxItemWidth = maxItemWidth,
                    itemHeight = itemHeight,
                    contentPadding = contentPadding,
                ) { injectableItem: AdInjectable<MoviePreview> ->
                    when (injectableItem) {
                        is InjectableAd -> {
                            ShowTimeNativeAd(
                                modifier = Modifier.fillMaxSize(),
                                ad = injectableItem.ad,
                                onAdLoaded = { ad -> onAdLoaded(injectableItem, ad) },
                                style = injectableItem.style
                            )
                        }

                        is InjectableContent<*> -> {
                            val movie = (injectableItem as InjectableContent<MoviePreview>).item
                            HeroItem(
                                title = movie.title,
                                imageUrl = movie.backdropImageUrl.ifEmpty { movie.posterImageUrl },
                                releaseDate = movie.displayReleaseDate,
                                voteAvg = movie.voteAvg,
                                onClick = { onMovieClicked(movie) },
                                overlayContent = { overlayContent?.invoke(movie) }
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
    showAppBar: Boolean,
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
            if (showAppBar) {
                HomePageAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    onSearchIconPressed = onSearchClicked,
                    onAccountIconPressed = onAccountClicked
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            } else {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            }

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

                // Center Large Item (Handles Dp.Unspecified smartly via weight)
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
    showAppBar: Boolean,
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
            if (showAppBar) {
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
            } else {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            }

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
