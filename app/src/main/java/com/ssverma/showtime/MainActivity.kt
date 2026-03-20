package com.ssverma.showtime

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.ads.ui.LocalAdConfigProvider
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.shared.ui.AppStateHolder
import com.ssverma.shared.ui.LocalAppStateHolder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var appStateHolder: AppStateHolder

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var adConfigProvider: AdConfigProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(
                LocalAppStateHolder provides appStateHolder,
                LocalAnalytics provides analytics,
                LocalAdConfigProvider provides adConfigProvider
            ) {
                ShowTime()
            }
        }
    }
}
