package com.ssverma.core.notifications

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDispatcher @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards NotificationProvider>
) : Notifications {

    override fun subscribeToTopic(topic: String) {
        providers.forEach { it.subscribeToTopic(topic) }
    }

    override fun unsubscribeFromTopic(topic: String) {
        providers.forEach { it.unsubscribeFromTopic(topic) }
    }

    override fun getToken(): Flow<String?> {
        return providers.map { it.getToken() }.merge()
    }
}
