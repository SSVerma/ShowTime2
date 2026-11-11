package com.ssverma.shared.ai.data.repository

import com.ssverma.shared.ai.a2ui.model.A2UiDocument
import com.ssverma.shared.ai.data.local.dao.CinephileChatDao
import com.ssverma.shared.ai.data.local.entity.CinephileMessageEntity
import com.ssverma.shared.ai.data.local.entity.CinephileThreadEntity
import com.ssverma.shared.ai.domain.model.CinephileChatMessage
import com.ssverma.shared.ai.domain.model.CinephileChatThread
import com.ssverma.shared.ai.domain.repository.CinephileChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CinephileChatRepositoryImpl @Inject constructor(
    private val chatDao: CinephileChatDao,
) : CinephileChatRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun getThreads(): Flow<List<CinephileChatThread>> {
        return chatDao.getThreads().map { entities ->
            entities.map { entity ->
                CinephileChatThread(
                    id = entity.id,
                    title = entity.title,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    override fun getMessages(threadId: String): Flow<List<CinephileChatMessage>> {
        return chatDao.getMessagesForThread(threadId).map { entities ->
            entities.map { entity ->
                if (entity.role == "USER") {
                    CinephileChatMessage.User(
                        id = entity.id,
                        threadId = entity.threadId,
                        text = entity.text,
                        timestamp = entity.timestamp
                    )
                } else {
                    val document = entity.a2uiJson?.let {
                        try {
                            json.decodeFromString<A2UiDocument>(it)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    CinephileChatMessage.Assistant(
                        id = entity.id,
                        threadId = entity.threadId,
                        text = entity.text,
                        document = document,
                        timestamp = entity.timestamp
                    )
                }
            }
        }
    }

    override suspend fun createThread(title: String, threadId: String): CinephileChatThread {
        val now = System.currentTimeMillis()
        val entity = CinephileThreadEntity(
            id = threadId,
            title = title,
            createdAt = now,
            updatedAt = now
        )
        chatDao.insertThread(entity)
        return CinephileChatThread(
            id = entity.id,
            title = entity.title,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override suspend fun saveUserMessage(
        threadId: String,
        text: String,
        messageId: String
    ): CinephileChatMessage.User {
        val now = System.currentTimeMillis()
        val existingThread = chatDao.getThreadById(threadId)
        if (existingThread != null) {
            chatDao.updateThread(existingThread.copy(updatedAt = now))
        } else {
            val title = text.take(40).trim().ifEmpty { "Chat" }
            chatDao.insertThread(
                CinephileThreadEntity(
                    id = threadId,
                    title = title,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }

        val messageEntity = CinephileMessageEntity(
            id = messageId,
            threadId = threadId,
            role = "USER",
            text = text,
            a2uiJson = null,
            timestamp = now
        )
        chatDao.insertMessage(messageEntity)

        return CinephileChatMessage.User(
            id = messageEntity.id,
            threadId = messageEntity.threadId,
            text = messageEntity.text,
            timestamp = messageEntity.timestamp
        )
    }

    override suspend fun saveAssistantMessage(
        threadId: String,
        text: String,
        document: A2UiDocument?,
        messageId: String
    ): CinephileChatMessage.Assistant {
        val now = System.currentTimeMillis()
        val a2uiJson = document?.let {
            try {
                json.encodeToString(it)
            } catch (_: Exception) {
                null
            }
        }

        val existingThread = chatDao.getThreadById(threadId)
        if (existingThread != null) {
            chatDao.updateThread(existingThread.copy(updatedAt = now))
        }

        val messageEntity = CinephileMessageEntity(
            id = messageId,
            threadId = threadId,
            role = "ASSISTANT",
            text = text,
            a2uiJson = a2uiJson,
            timestamp = now
        )
        chatDao.insertMessage(messageEntity)

        return CinephileChatMessage.Assistant(
            id = messageEntity.id,
            threadId = messageEntity.threadId,
            text = messageEntity.text,
            document = document,
            timestamp = messageEntity.timestamp
        )
    }

    override suspend fun deleteThread(threadId: String) {
        chatDao.deleteMessagesForThread(threadId)
        chatDao.deleteThread(threadId)
    }
}
