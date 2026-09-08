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
    private var pendingShowRequest: Pair<Activity, () -> Unit>? = null

    val isAdLoaded: Boolean
        get() = rewardedAd != null

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
                    // If a user was waiting for the ad to show, fall back gracefully to grant reward
                    val pending = pendingShowRequest
                    pendingShowRequest = null
                    pending?.second?.invoke()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isAdLoading = false
                    analytics.logEvent(AdAnalyticsEvent("rewarded_loaded"))

                    // If a user clicked "Watch Ad" while loading, immediately display it now!
                    val pending = pendingShowRequest
                    if (pending != null) {
                        pendingShowRequest = null
                        val (activity, onReward) = pending
                        if (!activity.isFinishing && !activity.isDestroyed) {
                            showLoadedAd(ad, activity, onReward)
                        } else {
                            onReward()
                        }
                    }
                }
            }
        )
    }

    fun showRewardedAdIfReady(
        activity: Activity,
        onUserEarnedReward: () -> Unit
    ) {
        if (!adConfigProvider.isAdsEnabled) {
            onUserEarnedReward()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            showLoadedAd(ad, activity, onUserEarnedReward)
        } else if (isAdLoading) {
            pendingShowRequest = activity to onUserEarnedReward
        } else {
            pendingShowRequest = activity to onUserEarnedReward
            loadAd()
        }
    }

    private fun showLoadedAd(
        ad: RewardedAd,
        activity: Activity,
        onUserEarnedReward: () -> Unit
    ) {
        rewardedAd = null
        var isRewardEarned = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                analytics.logEvent(AdAnalyticsEvent("rewarded_dismissed"))
                if (isRewardEarned) {
                    onUserEarnedReward()
                }
                loadAd() // Preload the next rewarded ad
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
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
    }
}
