package com.ssverma.core.ads

import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.ccm.AppConfigProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAdConfigProvider @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    private val billingRepository: BillingRepository,
) : AdConfigProvider {

    companion object {
        private const val REMOTE_KEY_ADS_ENABLED = "remote_ads_enabled"
    }

    override val isAdsEnabled: Boolean
        get() {
            val isRemotelyEnabled = appConfigProvider.getBoolean(
                key = REMOTE_KEY_ADS_ENABLED,
                defaultValue = true
            )

            val isProUser = billingRepository.isProActive.value

            // Show ads only if remotely enabled AND user is NOT a Pro subscriber
            return isRemotelyEnabled && !isProUser
        }

    override val bannerAdId: String
        get() = BuildConfig.ADMOB_BANNER_ID

    override val interstitialAdId: String
        get() = BuildConfig.ADMOB_INTERSTITIAL_ID

    override val nativeAdId: String
        get() = BuildConfig.ADMOB_NATIVE_ID

    override val appOpenAdId: String
        get() = BuildConfig.ADMOB_APP_OPEN_ID

    override val rewardedAdId: String
        get() = BuildConfig.ADMOB_REWARDED_ID
}
