package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
    onAdLoaded: (NativeAd) -> Unit,
    onAdFailed: () -> Unit = {},
    isFailed: Boolean = false,
    isAdsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!isAdsEnabled) return
    item(key = "in_viewport_native_ad_shelf") {
        AnimatedVisibility(
            visible = !isFailed,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(animationSpec = tween(250)) + shrinkVertically(
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            ShowTimeNativeAd(
                ad = nativeAd,
                onAdLoaded = onAdLoaded,
                onAdFailed = onAdFailed,
                style = NativeAdStyle.List,
                analyticsEventPrefix = "dashboard_native_ad",
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}
