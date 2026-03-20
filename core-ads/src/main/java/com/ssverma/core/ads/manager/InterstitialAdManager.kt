package com.ssverma.core.ads.manager

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.ssverma.core.ads.analytics.AdAnalyticsEvent
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.to
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialAdManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val analytics: Analytics,
    private val adConfigProvider: AdConfigProvider
) {
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    fun loadAd() {
        if (!adConfigProvider.isAdsEnabled || interstitialAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adConfigProvider.interstitialAdId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    analytics.logEvent(
                        AdAnalyticsEvent(
                            eventName = "interstitial_failed",
                            params = mapOf("message" to adError.message)
                        )
                    )
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    analytics.logEvent(AdAnalyticsEvent("interstitial_loaded"))
                }
            }
        )
    }

    fun showAdIfReady(activity: Activity, onAdDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad != null && adConfigProvider.isAdsEnabled) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    analytics.logEvent(AdAnalyticsEvent("interstitial_dismissed"))
                    onAdDismissed()
                    loadAd() // Preload the next one
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    analytics.logEvent(
                        AdAnalyticsEvent(
                            eventName = "interstitial_show_failed",
                            params = mapOf("message" to adError.message)
                        )
                    )
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    analytics.logEvent(AdAnalyticsEvent("interstitial_impression"))
                }

                override fun onAdClicked() {
                    analytics.logEvent(AdAnalyticsEvent("interstitial_clicked"))
                }
            }
            ad.show(activity)
        } else {
            // Ad wasn't ready or was disabled. Proceed immediately.
            onAdDismissed()
            loadAd()
        }
    }
}
