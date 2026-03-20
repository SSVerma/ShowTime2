package com.ssverma.showtime

import android.app.Application
import com.ssverma.core.ads.AdInitializer
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.showtime.analytics.AnalyticsSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShowTimeApp : Application() {
    @Inject
    lateinit var appConfigProvider: AppConfigProvider

    @Inject
    lateinit var analyticsSyncManager: AnalyticsSyncManager

    @Inject
    lateinit var adInitializer: AdInitializer

    override fun onCreate() {
        super.onCreate()
        appConfigProvider.fetchAndActivate()
        analyticsSyncManager.startSync()
        adInitializer.initialize()
    }
}
