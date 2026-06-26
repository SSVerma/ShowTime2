package com.ssverma.core.notifications.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.ssverma.core.notifications.NotificationProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseNotificationProvider @Inject constructor() : NotificationProvider {

    override fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
    }

    override fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
    }

    override fun getToken(): Flow<String?> = callbackFlow {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                trySend(task.result)
            } else {
                trySend(null)
            }
            close()
        }
        awaitClose { }
    }
}
