package com.ssverma.core.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.ssverma.core.ads.config.AdConfigProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdInitializer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val adConfigProvider: AdConfigProvider
) {
    fun initialize() {
        if (!adConfigProvider.isAdsEnabled) return

        MobileAds.initialize(context) { initializationStatus ->
            // Ready to load ads
        }
    }
}
