package com.ssverma.core.notifications

import kotlinx.coroutines.flow.Flow

interface NotificationProvider {
    fun subscribeToTopic(topic: String)

    fun unsubscribeFromTopic(topic: String)

    fun getToken(): Flow<String?>
}
