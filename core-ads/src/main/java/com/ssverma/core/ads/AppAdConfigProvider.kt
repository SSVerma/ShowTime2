package com.ssverma.core.ads

import com.ssverma.core.ads.config.AdConfigProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAdConfigProvider @Inject constructor() : AdConfigProvider {

    // Tie this to Remote Config or a Premium Subscription flag later!
    override val isAdsEnabled: Boolean = true

    override val bannerAdId: String
        get() = BuildConfig.ADMOB_BANNER_ID

    override val interstitialAdId: String
        get() = BuildConfig.ADMOB_INTERSTITIAL_ID
}
