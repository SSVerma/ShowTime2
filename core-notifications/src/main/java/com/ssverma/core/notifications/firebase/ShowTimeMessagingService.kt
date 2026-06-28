package com.ssverma.core.notifications.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssverma.core.notifications.ShowTimeNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowTimeMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: ShowTimeNotificationManager

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "From: ${message.from}")

        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            notificationManager.showNotification(
                title = it.title,
                message = it.body,
                imageUrl = it.imageUrl?.toString(),
                deepLink = it.link?.toString()
            )
        }

        // Also if you intend on generating your own notifications as a result of a data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            val title = message.data["title"]
            val body = message.data["body"]
            val image = message.data["image"]
            val deepLink = message.data["deepLink"]

            if (title != null || body != null) {
                notificationManager.showNotification(
                    title = title,
                    message = body,
                    imageUrl = image,
                    deepLink = deepLink
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
    }

    companion object {
        private const val TAG = "ShowTimeMessaging"
    }
}
