package com.ssverma.showtime

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
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
import com.ssverma.core.ads.manager.AppOpenAdManager
import com.ssverma.core.ads.ui.LocalAdConfigProvider
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.notifications.LocalNotificationManager
import com.ssverma.core.notifications.ShowTimeNotificationManager
import com.ssverma.shared.ui.AppStateHolder
import com.ssverma.shared.ui.LocalAppStateHolder
import com.ssverma.core.di.AppScoped
import com.ssverma.showtime.navigation.ShowTimeDeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    @AppScoped
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var appStateHolder: AppStateHolder

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var adConfigProvider: AdConfigProvider

    @Inject
    lateinit var appOpenAdManager: AppOpenAdManager

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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            navigationEventDispatcherOwner.navigationEventDispatcher.addInput(
                OnBackInvokedDefaultInput(onBackInvokedDispatcher)
            )
        }

        deepLinkKey.value = extractNavKey(intent)

        setContent {
            val backInput = remember { DirectNavigationEventInput() }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                BackHandler {
                    backInput.backCompleted()
                }
            }

            LaunchedEffect(backInput) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
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

    override fun onResume() {
        super.onResume()
        appOpenAdManager.showAdIfAvailable(this)
        appScope.launch {
            com.ssverma.showtime.widget.WidgetUpdateHelper.updateAllWidgets(this@MainActivity)
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
