package com.ssverma.core.ads.config

import com.google.android.gms.ads.AdSize
import kotlinx.coroutines.flow.Flow

/**
 * The contract that the app module must fulfill.
 * Controls the kill-switch and supplies the correct IDs.
 */
interface AdConfigProvider {
    val isAdsEnabled: Boolean
    val bannerAdId: String
    val interstitialAdId: String
    val nativeAdId: String
    val appOpenAdId: String
}

/**
 * Domain representation of Ad Sizes so the UI isn't tightly coupled to AdMob.
 */
sealed class BannerAdSize(val adMobSize: AdSize) {
    data object Standard : BannerAdSize(adMobSize = AdSize.BANNER)           // 320x50
    data object Large : BannerAdSize(adMobSize = AdSize.LARGE_BANNER)        // 320x100
    data object MediumRectangle : BannerAdSize(adMobSize = AdSize.MEDIUM_RECTANGLE) // 300x250
    data object FullBanner : BannerAdSize(adMobSize = AdSize.FULL_BANNER)    // 468x60
    data object Leaderboard : BannerAdSize(adMobSize = AdSize.LEADERBOARD)   // 728x90
}
