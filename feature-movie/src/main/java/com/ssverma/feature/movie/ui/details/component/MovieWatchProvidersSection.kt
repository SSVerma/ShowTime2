package com.ssverma.feature.movie.ui.details.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.ui.component.section.WatchProvidersSection

fun LazyListScope.movieWatchProvidersSection(
    movie: Movie,
    watchProviderRegion: String,
    watchProviderAd: NativeAd?,
    onWatchProviderClick: (ProviderInfo) -> Unit,
    onWatchProviderWithCategoryClick: (ProviderInfo, String) -> Unit,
    onJustWatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val watchProvider = movie.watchProviders[watchProviderRegion]
    if (watchProvider != null && watchProvider.hasProviders) {
        item(key = "movie_watch_providers", contentType = "watch_providers") {
            WatchProvidersSection(
                watchProvider = watchProvider,
                adContent = watchProviderAd?.let { ad ->
                    {
                        ShowTimeNativeAd(
                            ad = ad,
                            loadInternally = false,
                            style = NativeAdStyle.CircularLogo,
                            modifier = Modifier.size(44.dp),
                            analyticsEventPrefix = "movie_details_watch_provider"
                        )
                    }
                },
                onWatchProviderClick = onWatchProviderClick,
                onWatchProviderWithCategoryClick = onWatchProviderWithCategoryClick,
                onJustWatchClick = onJustWatchClick,
                modifier = modifier
            )
        }
    }
}
