package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ui.UiState
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.ui.component.AppHeroCarousel
import com.ssverma.shared.ui.component.HeroItem
import com.ssverma.shared.ui.component.media.menu.MediaOmniActionMenu
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
                        HeroItem(
                            title = spotlightItem.title,
                            imageUrl = spotlightItem.backdropImageUrl.ifEmpty { spotlightItem.posterImageUrl },
                            formatBadge = stringResource(
                                id = if (spotlightItem.mediaType == MediaType.Movie) R.string.movie_badge else R.string.tv_badge
                            ),
                            releaseDate = spotlightItem.displayDate,
                            voteAvg = spotlightItem.voteAvg,
                            onClick = {
                                if (spotlightItem.mediaType == MediaType.Movie) {
                                    onMovieClick(spotlightItem)
                                } else {
                                    onTvShowClick(spotlightItem)
                                }
                            },
                            overlayContent = {
                                MediaOmniActionMenu(
                                    mediaId = spotlightItem.id,
                                    mediaType = spotlightItem.mediaType,
                                    title = spotlightItem.title,
                                    posterImageUrl = spotlightItem.posterImageUrl,
                                    backdropImageUrl = spotlightItem.backdropImageUrl,
                                    voteAvg = spotlightItem.voteAvg,
                                    releaseDate = spotlightItem.displayDate.orEmpty(),
                                    isOverPoster = true,
                                    onShowFeedback = onShowFeedback
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
