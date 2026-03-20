package com.ssverma.core.ads.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.ssverma.core.ads.analytics.AdAnalyticsEvent
import com.ssverma.core.ads.config.BannerAdSize
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.to
import com.ssverma.core.analytics.ui.LocalAnalytics

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adSize: BannerAdSize = BannerAdSize.Standard,
    analyticsEventPrefix: String = "banner_ad"
) {
    val adConfigProvider = LocalAdConfigProvider.current

    // THE KILL SWITCH
    if (!adConfigProvider.isAdsEnabled) return

    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val analytics = LocalAnalytics.current

    val adView = remember {
        AdView(context).apply {
            setAdSize(adSize.adMobSize)
            adUnitId = adConfigProvider.bannerAdId
            adListener = createTrackingAdListener(
                analytics = analytics,
                prefix = analyticsEventPrefix
            )
        }
    }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        adView.loadAd(AdRequest.Builder().build())

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { adView }
    )
}

private fun createTrackingAdListener(analytics: Analytics, prefix: String) = object : AdListener() {
    override fun onAdLoaded() {
        analytics.logEvent(AdAnalyticsEvent("${prefix}_loaded"))
    }

    override fun onAdFailedToLoad(error: LoadAdError) {
        analytics.logEvent(
            AdAnalyticsEvent(
                eventName = "${prefix}_failed",
                params = mapOf("error_code" to error.code, "message" to error.message)
            )
        )
    }

    override fun onAdOpened() {
        analytics.logEvent(AdAnalyticsEvent("${prefix}_opened"))
    }

    override fun onAdClicked() {
        analytics.logEvent(AdAnalyticsEvent("${prefix}_clicked"))
    }

    override fun onAdImpression() {
        analytics.logEvent(AdAnalyticsEvent("${prefix}_impression"))
    }
}
