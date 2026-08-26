package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.UiState
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.ui.component.AppHeroCarousel
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.dashboard.TrendingSpotlightItem

@OptIn(ExperimentalMaterial3Api::class)
fun LazyListScope.trendingSpotlightShelf(
    trendingState: UiState<List<AdInjectable<TrendingSpotlightItem>>, MovieFailure>,
    carouselState: CarouselState,
    onMovieClick: (TrendingSpotlightItem) -> Unit,
    onTvShowClick: (TrendingSpotlightItem) -> Unit,
    onAdLoaded: (InjectableAd, NativeAd) -> Unit,
    onRetry: () -> Unit,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null
) {
    item(key = "trending_spotlight_shelf") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            AppHeroCarousel(
                uiState = trendingState,
                carouselState = carouselState,
                onRetry = onRetry,
                itemHeight = 220.dp,
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) { injectableItem: AdInjectable<TrendingSpotlightItem> ->
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
                        val spotlightItem =
                            (injectableItem as InjectableContent<TrendingSpotlightItem>).item
                        TrendingSpotlightCard(
                            item = spotlightItem,
                            onClick = {
                                if (spotlightItem.mediaType == MediaType.Movie) {
                                    onMovieClick(spotlightItem)
                                } else {
                                    onTvShowClick(spotlightItem)
                                }
                            },
                            onShowFeedback = onShowFeedback
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingSpotlightCard(
    item: TrendingSpotlightItem,
    onClick: () -> Unit,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
    ) {
        NetworkImage(
            url = item.backdropImageUrl.ifEmpty { item.posterImageUrl },
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 100f
                    )
                )
        )

        // Content Info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (item.displayDate != null) {
                    Text(
                        text = item.displayDate.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                if (item.voteAvg > 0f) {
                    Text(
                        text = "★ ${String.format("%.1f", item.voteAvg)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // Top-Left Format Badge (Dynamic: Movie vs TV Series)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(
                        id = if (item.mediaType == MediaType.Movie) R.string.movie_badge else R.string.tv_badge
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Overlay Action (Watchlist/Favorite)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            MediaStatsAction(
                mediaType = item.mediaType,
                mediaId = item.id,
                title = item.title,
                posterImageUrl = item.posterImageUrl,
                backdropImageUrl = item.backdropImageUrl,
                voteAvg = item.voteAvg,
                releaseDate = item.displayDate.orEmpty(),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                onShowFeedback = onShowFeedback,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
