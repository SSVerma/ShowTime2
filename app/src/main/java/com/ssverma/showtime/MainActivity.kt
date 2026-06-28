package com.ssverma.showtime

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.OnBackInvokedDefaultInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.ads.ui.LocalAdConfigProvider
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.notifications.LocalNotificationManager
import com.ssverma.core.notifications.ShowTimeNotificationManager
import com.ssverma.shared.ui.AppStateHolder
import com.ssverma.shared.ui.LocalAppStateHolder
import com.ssverma.showtime.navigation.ShowTimeDeepLinkHandler
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

    @Inject
    lateinit var notificationManager: ShowTimeNotificationManager

    private val deepLinkKey = mutableStateOf<NavKey?>(null)

    private val navigationEventDispatcherOwner = object : NavigationEventDispatcherOwner {
        override val navigationEventDispatcher = NavigationEventDispatcher(
            onBackCompletedFallback = {
                finish()
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            navigationEventDispatcherOwner.navigationEventDispatcher.addInput(
                OnBackInvokedDefaultInput(onBackInvokedDispatcher)
            )
        }

        deepLinkKey.value = extractNavKey(intent)

        setContent {
            val backInput = remember { DirectNavigationEventInput() }
            if (android.os.Build.VERSION.SDK_INT < 33) {
                BackHandler {
                    backInput.backCompleted()
                }
            }

            LaunchedEffect(backInput) {
                if (android.os.Build.VERSION.SDK_INT < 33) {
                    navigationEventDispatcherOwner.navigationEventDispatcher.addInput(backInput)
                }
            }

            CompositionLocalProvider(
                LocalAppStateHolder provides appStateHolder,
                LocalAnalytics provides analytics,
                LocalAdConfigProvider provides adConfigProvider,
                LocalNotificationManager provides notificationManager,
                LocalNavigationEventDispatcherOwner provides navigationEventDispatcherOwner
            ) {
                ShowTime(initialDeepLinkKey = deepLinkKey.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkKey.value = extractNavKey(intent)
    }

    private fun extractNavKey(intent: Intent): NavKey? {
        // 1. Check data URI (from adb or PendingIntent)
        intent.data?.let {
            val navKey = ShowTimeDeepLinkHandler.parse(it)
            if (navKey != null) return navKey
        }

        // 2. Check extras (from FCM background delivery)
        intent.extras?.getString("deepLink")?.let {
            val navKey = ShowTimeDeepLinkHandler.parse(it)
            if (navKey != null) return navKey
        }

        return null
    }
}
