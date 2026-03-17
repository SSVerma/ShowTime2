package com.ssverma.showtime

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.ssverma.showtime.analytics.AnalyticsSyncManager
import javax.inject.Inject

@HiltAndroidApp
class ShowTimeApp : Application() {
    @Inject
    lateinit var analyticsSyncManager: AnalyticsSyncManager

    override fun onCreate() {
        super.onCreate()
        analyticsSyncManager.startSync()
    }
}