package com.ssverma.showtime.util

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.ssverma.showtime.R

/**
 * Configures edge-to-edge system bar styles and ensures desktop/freeform window caption bars
 * retain an opaque contrasting surface so system controls remain clearly visible.
 */
fun applyShowTimeSystemBars(
    activity: ComponentActivity,
    isDarkTheme: Boolean,
    hasCaptionBarInset: Boolean = false
) {
    val rootCaptionTop = ViewCompat.getRootWindowInsets(activity.window.decorView)
        ?.getInsets(WindowInsetsCompat.Type.captionBar())
        ?.top ?: 0
    val hasDesktopCaption = hasCaptionBarInset ||
            rootCaptionTop > 0 ||
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PC)

    val statusBarStyle = if (hasDesktopCaption) {
        val captionColor = ContextCompat.getColor(
            activity,
            if (isDarkTheme) R.color.desktop_caption_bar_dark else R.color.desktop_caption_bar_light
        )
        if (isDarkTheme) {
            SystemBarStyle.dark(scrim = captionColor)
        } else {
            SystemBarStyle.light(scrim = captionColor, darkScrim = captionColor)
        }
    } else {
        SystemBarStyle.auto(
            lightScrim = Color.TRANSPARENT,
            darkScrim = Color.TRANSPARENT,
            detectDarkMode = { _ -> isDarkTheme }
        )
    }

    val navigationBarStyle = SystemBarStyle.auto(
        lightScrim = Color.TRANSPARENT,
        darkScrim = Color.TRANSPARENT,
        detectDarkMode = { _ -> isDarkTheme }
    )

    activity.enableEdgeToEdge(
        statusBarStyle = statusBarStyle,
        navigationBarStyle = navigationBarStyle
    )

    val window = activity.window
    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = !isDarkTheme
        isAppearanceLightNavigationBars = !isDarkTheme
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        val lightCaptionFlag = WindowInsetsController.APPEARANCE_LIGHT_CAPTION_BARS
        val transparentCaptionFlag =
            WindowInsetsController.APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND
        val desiredAppearance = if (!isDarkTheme) lightCaptionFlag else 0
        val mask = if (hasDesktopCaption) {
            lightCaptionFlag or transparentCaptionFlag
        } else {
            lightCaptionFlag
        }
        window.insetsController?.setSystemBarsAppearance(desiredAppearance, mask)
    }
}
