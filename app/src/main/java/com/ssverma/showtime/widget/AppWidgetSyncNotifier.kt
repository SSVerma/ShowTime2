package com.ssverma.showtime.widget

import android.content.Context
import android.util.Log
import com.ssverma.core.di.AppScoped
import com.ssverma.shared.domain.notifier.WidgetSyncNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppWidgetSyncNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:AppScoped private val scope: CoroutineScope
) : WidgetSyncNotifier {

    override fun notifyWidgetDataChanged() {
        Log.d("AppWidgetSyncNotifier", "notifyWidgetDataChanged called")
        scope.launch(Dispatchers.IO) {
            try {
                WidgetUpdateHelper.updateAllWidgets(context)
                Log.d("AppWidgetSyncNotifier", "Widgets successfully updated")
            } catch (e: Exception) {
                Log.e("AppWidgetSyncNotifier", "Error updating widgets", e)
            }
        }
    }
}
