package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.ui.component.section.WatchProvidersSection

fun LazyListScope.tvWatchProvidersSection(
    tvShow: TvShow,
    watchProviderRegion: String,
    watchProviderAd: NativeAd?,
    onWatchProviderClick: (ProviderInfo) -> Unit,
    onWatchProviderWithCategoryClick: (ProviderInfo, String) -> Unit,
    onJustWatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val watchProvider = tvShow.watchProviders[watchProviderRegion]
    if (watchProvider != null && watchProvider.hasProviders) {
        item(key = "tv_watch_providers", contentType = "watch_providers") {
            WatchProvidersSection(
                watchProvider = watchProvider,
                modifier = modifier,
                adContent = {
                    ShowTimeNativeAd(
                        ad = watchProviderAd,
                        loadInternally = false,
                        style = NativeAdStyle.CircularLogo,
                        modifier = Modifier.size(44.dp),
                        analyticsEventPrefix = "tv_details_watch_provider"
                    )
                },
                onWatchProviderClick = onWatchProviderClick,
                onWatchProviderWithCategoryClick = onWatchProviderWithCategoryClick,
                onJustWatchClick = onJustWatchClick
            )
        }
    }
}
