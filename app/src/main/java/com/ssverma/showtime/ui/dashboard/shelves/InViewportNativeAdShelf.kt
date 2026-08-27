package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle

fun LazyListScope.inViewportNativeAdShelf(
    nativeAd: NativeAd?,
    onAdLoaded: (NativeAd) -> Unit
) {
    item(key = "in_viewport_native_ad_shelf") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 18.dp)
        ) {
            ShowTimeNativeAd(
                ad = nativeAd,
                onAdLoaded = onAdLoaded,
                style = NativeAdStyle.List,
                analyticsEventPrefix = "dashboard_native_ad"
            )
        }
    }
}
