package com.ssverma.core.ads.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.ssverma.core.ads.analytics.AdAnalyticsEvent
import com.ssverma.core.analytics.to
import com.ssverma.core.analytics.ui.LocalAnalytics

/**
 * Purely fetches the AdMob NativeAd data payload. No UI rendering.
 */
@Composable
fun rememberNativeAd(
    loadAd: Boolean = true,
    analyticsEventPrefix: String = "native_ad",
    onAdLoaded: (NativeAd) -> Unit = {},
    onAdFailedToLoad: (LoadAdError) -> Unit = {}
): NativeAd? {
    val adConfigProvider = LocalAdConfigProvider.current

    if (!adConfigProvider.isAdsEnabled) return null

    val context = LocalContext.current
    val analytics = LocalAnalytics.current

    val currentOnLoaded by rememberUpdatedState(onAdLoaded)
    val currentOnFailed by rememberUpdatedState(onAdFailedToLoad)

    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(loadAd) {
        if (!loadAd) return@DisposableEffect onDispose {}

        val adLoader = AdLoader.Builder(context, adConfigProvider.nativeAdId)
            .forNativeAd { ad ->
                nativeAd?.destroy() // Clean up old ad if reloading
                nativeAd = ad
                currentOnLoaded(ad)
                analytics.logEvent(AdAnalyticsEvent("${analyticsEventPrefix}_loaded"))
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    currentOnFailed(error)
                    analytics.logEvent(
                        AdAnalyticsEvent(
                            eventName = "${analyticsEventPrefix}_failed",
                            params = mapOf("error_code" to error.code, "message" to error.message)
                        )
                    )
                }

                override fun onAdClicked() {
                    analytics.logEvent(AdAnalyticsEvent("${analyticsEventPrefix}_clicked"))
                }

                override fun onAdImpression() {
                    analytics.logEvent(AdAnalyticsEvent("${analyticsEventPrefix}_impression"))
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
        }
    }

    return nativeAd
}
