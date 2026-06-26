package com.ssverma.showtime.notifications

import android.util.Log
import com.ssverma.core.notifications.Notifications
import com.ssverma.core.notifications.ShowTimeNotificationManager
import com.ssverma.shared.domain.repository.AppConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSyncManager @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
    private val notifications: Notifications,
    private val notificationManager: ShowTimeNotificationManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun startSync() {
        appConfigRepository.isNotificationsEnabled
            .onEach { enabled ->
                if (enabled) {
                    Log.d(TAG, "Notifications enabled. Initializing...")
                    notificationManager.createNotificationChannels()
                    notifications.subscribeToTopic(TOPIC_ALL_USERS)
                } else {
                    Log.d(TAG, "Notifications disabled. Unsubscribing...")
                    notifications.unsubscribeFromTopic(TOPIC_ALL_USERS)
                }
            }
            .launchIn(scope)
    }

    companion object {
        private const val TAG = "NotificationSync"
        private const val TOPIC_ALL_USERS = "all_users"
    }
}
