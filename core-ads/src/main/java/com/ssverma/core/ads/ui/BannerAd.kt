package com.ssverma.core.ads.ui

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
    adView: AdView? = null,
    adSize: BannerAdSize = BannerAdSize.Standard,
    analyticsEventPrefix: String = "banner_ad",
    destroyOnDispose: Boolean = true,
    onAdLoaded: (AdView) -> Unit = {},
    onAdFailedToLoad: (LoadAdError) -> Unit = {}
) {
    val adConfigProvider = LocalAdConfigProvider.current

    // THE KILL SWITCH
    if (!adConfigProvider.isAdsEnabled) return

    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val analytics = LocalAnalytics.current

    // Prevent Stale State memory leaks in LazyLists
    val currentOnAdLoaded by rememberUpdatedState(onAdLoaded)
    val currentOnAdFailed by rememberUpdatedState(onAdFailedToLoad)

    val effectiveAdView = remember(adView) {
        adView ?: AdView(context).apply {
            setAdSize(adSize.adMobSize)
            adUnitId = adConfigProvider.bannerAdId
            adListener = createTrackingAdListener(
                analytics = analytics,
                prefix = analyticsEventPrefix,
                onAdLoaded = { currentOnAdLoaded(this) },
                onAdFailedToLoad = { currentOnAdFailed(it) }
            )
        }
    }

    DisposableEffect(lifecycleOwner, effectiveAdView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> effectiveAdView.resume()
                Lifecycle.Event.ON_PAUSE -> effectiveAdView.pause()
                Lifecycle.Event.ON_DESTROY -> {
                    if (destroyOnDispose) effectiveAdView.destroy()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Only load if not already loading/loaded
        if (effectiveAdView.responseInfo == null) {
            effectiveAdView.loadAd(AdRequest.Builder().build())
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (destroyOnDispose) effectiveAdView.destroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            (effectiveAdView.parent as? ViewGroup)?.removeView(effectiveAdView)
            effectiveAdView
        }
    )
}

private fun createTrackingAdListener(
    analytics: Analytics,
    prefix: String,
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: (LoadAdError) -> Unit
) = object : AdListener() {
    override fun onAdLoaded() {
        onAdLoaded()
        analytics.logEvent(AdAnalyticsEvent("${prefix}_loaded"))
    }

    override fun onAdFailedToLoad(error: LoadAdError) {
        onAdFailedToLoad(error)
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
