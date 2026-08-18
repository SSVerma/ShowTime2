package com.ssverma.core.ads.manager

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.ssverma.core.ads.analytics.AdAnalyticsEvent
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.to
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val analytics: Analytics,
    private val adConfigProvider: AdConfigProvider
) {
    companion object {
        private const val FOUR_HOURS_IN_MILLIS = 4 * 60 * 60 * 1000L
    }

    private var appOpenAd: AppOpenAd? = null
    private var isAdLoading = false
    private var isShowingAd = false
    private var loadTime: Long = 0
    private var lastDismissedTime: Long = 0

    fun loadAd() {
        if (!adConfigProvider.isAdsEnabled || isAdAvailable() || isAdLoading) return

        isAdLoading = true
        val request = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            adConfigProvider.appOpenAdId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAdLoading = false
                    loadTime = Date().time
                    analytics.logEvent(AdAnalyticsEvent("app_open_loaded"))
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                    isAdLoading = false
                    analytics.logEvent(
                        AdAnalyticsEvent(
                            eventName = "app_open_failed",
                            params = mapOf("message" to loadAdError.message)
                        )
                    )
                }
            }
        )
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanFourHoursAgo()
    }

    private fun wasLoadTimeLessThanFourHoursAgo(): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * 4
    }

    private fun canShowAdFrequency(): Boolean {
        if (lastDismissedTime == 0L) return true
        return (System.currentTimeMillis() - lastDismissedTime) >= FOUR_HOURS_IN_MILLIS
    }

    fun showAdIfAvailable(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (isShowingAd) {
            onAdDismissed()
            return
        }

        if (!adConfigProvider.isAdsEnabled || !canShowAdFrequency()) {
            onAdDismissed()
            return
        }

        if (!isAdAvailable()) {
            onAdDismissed()
            loadAd()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                lastDismissedTime = System.currentTimeMillis()
                analytics.logEvent(AdAnalyticsEvent("app_open_dismissed"))
                onAdDismissed()
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                analytics.logEvent(
                    AdAnalyticsEvent(
                        eventName = "app_open_show_failed",
                        params = mapOf("message" to adError.message)
                    )
                )
                onAdDismissed()
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                analytics.logEvent(AdAnalyticsEvent("app_open_impression"))
            }

            override fun onAdClicked() {
                analytics.logEvent(AdAnalyticsEvent("app_open_clicked"))
            }
        }

        isShowingAd = true
        appOpenAd?.show(activity)
    }
}
