package com.ssverma.common.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build

object WidgetPinHelper {
    const val UP_NEXT_RECEIVER_CLASS = "com.ssverma.showtime.widget.upnext.UpNextWidgetReceiver"
    const val WATCHLIST_RECEIVER_CLASS =
        "com.ssverma.showtime.widget.watchlist.WatchlistWidgetReceiver"

    /**
     * Checks if the device and default launcher support programmatic widget pinning (API 26+).
     */
    fun isPinningSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return false
        return appWidgetManager.isRequestPinAppWidgetSupported
    }

    /**
     * Dispatches a request to the default launcher to pin the specified widget.
     * The launcher will display its native confirmation / placement dialog.
     */
    fun pinWidget(
        context: Context,
        receiverClassName: String,
        successCallback: PendingIntent? = null
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return false
        if (!appWidgetManager.isRequestPinAppWidgetSupported) return false

        val componentName = ComponentName(context.packageName, receiverClassName)
        return appWidgetManager.requestPinAppWidget(componentName, null, successCallback)
    }
}
