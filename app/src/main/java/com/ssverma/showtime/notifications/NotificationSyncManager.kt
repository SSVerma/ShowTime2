package com.ssverma.showtime.notifications

import android.content.Context
import com.ssverma.core.notifications.Notifications
import com.ssverma.core.notifications.ShowTimeNotificationManager
import com.ssverma.shared.data.worker.ReleaseRadarWorker
import com.ssverma.shared.domain.repository.AppConfigRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages reactive synchronization between user/remote notification preferences and background scheduling.
 */
@Singleton
class NotificationSyncManager @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
    private val notifications: Notifications,
    private val notificationManager: ShowTimeNotificationManager,
    @param:ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Observes notification and release radar preferences to dynamically register notification channels,
     * manage topic subscriptions, and schedule or cancel background workers.
     */
    fun startSync() {
        appConfigRepository.isNotificationsEnabled
            .onEach { enabled ->
                if (enabled) {
                    notificationManager.createNotificationChannels()
                    notifications.subscribeToTopic(TOPIC_ALL_USERS)
                } else {
                    notifications.unsubscribeFromTopic(TOPIC_ALL_USERS)
                }
            }
            .launchIn(scope)

        combine(
            appConfigRepository.isNotificationsEnabled,
            appConfigRepository.isReleaseRadarEnabled
        ) { notificationsEnabled, radarEnabled ->
            notificationsEnabled && radarEnabled
        }
            .onEach { enabled ->
                if (enabled) {
                    ReleaseRadarWorker.schedule(context)
                } else {
                    ReleaseRadarWorker.cancel(context)
                }
            }
            .launchIn(scope)
    }

    companion object {
        private const val TOPIC_ALL_USERS = "all_users"
    }
}
