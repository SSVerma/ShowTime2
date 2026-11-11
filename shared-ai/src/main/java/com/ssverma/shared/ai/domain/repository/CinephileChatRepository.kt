package com.ssverma.shared.ai.domain.repository

import com.ssverma.shared.ai.a2ui.model.A2UiDocument
import com.ssverma.shared.ai.domain.model.CinephileChatMessage
import com.ssverma.shared.ai.domain.model.CinephileChatThread
import kotlinx.coroutines.flow.Flow

interface CinephileChatRepository {
    fun getThreads(): Flow<List<CinephileChatThread>>
    fun getMessages(threadId: String): Flow<List<CinephileChatMessage>>
    suspend fun createThread(
        title: String,
        threadId: String = java.util.UUID.randomUUID().toString()
    ): CinephileChatThread

    suspend fun saveUserMessage(
        threadId: String,
        text: String,
        messageId: String = java.util.UUID.randomUUID().toString()
    ): CinephileChatMessage.User

    suspend fun saveAssistantMessage(
        threadId: String,
        text: String,
        document: A2UiDocument?,
        messageId: String = java.util.UUID.randomUUID().toString()
    ): CinephileChatMessage.Assistant

    suspend fun deleteThread(threadId: String)
}
