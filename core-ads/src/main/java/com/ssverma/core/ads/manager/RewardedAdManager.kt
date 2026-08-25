package com.ssverma.core.ads.manager

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.ssverma.core.ads.analytics.AdAnalyticsEvent
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.to
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val analytics: Analytics,
    private val adConfigProvider: AdConfigProvider
) {
    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun loadAd() {
        if (!adConfigProvider.isAdsEnabled || rewardedAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            adConfigProvider.rewardedAdId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    isAdLoading = false
                    analytics.logEvent(
                        AdAnalyticsEvent(
                            eventName = "rewarded_failed",
                            params = mapOf("message" to adError.message)
                        )
                    )
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isAdLoading = false
                    analytics.logEvent(AdAnalyticsEvent("rewarded_loaded"))
                }
            }
        )
    }

    fun showRewardedAdIfReady(
        activity: Activity,
        onUserEarnedReward: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null && adConfigProvider.isAdsEnabled) {
            var isRewardEarned = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    analytics.logEvent(AdAnalyticsEvent("rewarded_dismissed"))
                    if (isRewardEarned) {
                        onUserEarnedReward()
                    }
                    loadAd() // Preload the next rewarded ad
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    analytics.logEvent(
                        AdAnalyticsEvent(
                            eventName = "rewarded_show_failed",
                            params = mapOf("message" to adError.message)
                        )
                    )
                    // If ad failed to display, gracefully grant reward so user flow isn't broken
                    onUserEarnedReward()
                    loadAd()
                }

                override fun onAdShowedFullScreenContent() {
                    analytics.logEvent(AdAnalyticsEvent("rewarded_impression"))
                }

                override fun onAdClicked() {
                    analytics.logEvent(AdAnalyticsEvent("rewarded_clicked"))
                }
            }

            ad.show(activity) { rewardItem ->
                isRewardEarned = true
                analytics.logEvent(
                    AdAnalyticsEvent(
                        eventName = "rewarded_earned",
                        params = mapOf(
                            "type" to rewardItem.type,
                            "amount" to rewardItem.amount.toString()
                        )
                    )
                )
            }
        } else {
            // Ads disabled (e.g. Pro subscriber) or ad not available yet -> grant reward directly
            onUserEarnedReward()
            loadAd()
        }
    }
}
