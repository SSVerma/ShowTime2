package com.ssverma.shared.ai.data.repository

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.ai.a2ui.model.A2UiComponent
import com.ssverma.shared.ai.a2ui.model.A2UiDocument
import com.ssverma.shared.ai.data.local.dao.CinephileChatDao
import com.ssverma.shared.ai.data.local.entity.CinephileMessageEntity
import com.ssverma.shared.ai.data.local.entity.CinephileThreadEntity
import com.ssverma.shared.ai.domain.model.CinephileChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CinephileChatRepositoryTest {

    private lateinit var fakeDao: FakeChatDao
    private lateinit var repository: CinephileChatRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeChatDao()
        repository = CinephileChatRepositoryImpl(chatDao = fakeDao)
    }

    @Test
    fun `createThread persists thread with updated timestamps`() = runTest {
        val thread = repository.createThread(title = "Sci-Fi recommendations", threadId = "t1")
        assertThat(thread.id).isEqualTo("t1")
        assertThat(thread.title).isEqualTo("Sci-Fi recommendations")

        val stored = repository.getThreads().first()
        assertThat(stored).hasSize(1)
        assertThat(stored.first().id).isEqualTo("t1")
    }

    @Test
    fun `saveUserMessage and saveAssistantMessage persist and decode messages correctly`() =
        runTest {
            repository.createThread(title = "Action Movies", threadId = "thread-123")

            repository.saveUserMessage(
                threadId = "thread-123",
                text = "Show me John Wick",
                messageId = "m1"
            )

            val doc = A2UiDocument(
                id = "doc-1",
                title = "Action Picks",
                components = listOf(
                    A2UiComponent.Text(id = "text-1", content = "Here is John Wick")
                )
            )
            repository.saveAssistantMessage(
                threadId = "thread-123",
                text = "",
                document = doc,
                messageId = "m2"
            )

            val messages = repository.getMessages("thread-123").first()
            assertThat(messages).hasSize(2)

            val userMsg = messages[0] as CinephileChatMessage.User
            assertThat(userMsg.text).isEqualTo("Show me John Wick")

            val assistantMsg = messages[1] as CinephileChatMessage.Assistant
            assertThat(assistantMsg.document).isNotNull()
            assertThat(assistantMsg.document?.title).isEqualTo("Action Picks")
        }

    @Test
    fun `deleteThread removes thread and its messages`() = runTest {
        repository.createThread(title = "To Delete", threadId = "del-1")
        repository.saveUserMessage(threadId = "del-1", text = "Hello", messageId = "msg-1")

        repository.deleteThread("del-1")

        assertThat(repository.getThreads().first()).isEmpty()
        assertThat(repository.getMessages("del-1").first()).isEmpty()
    }

    private class FakeChatDao : CinephileChatDao {
        private val threadsFlow = MutableStateFlow<Map<String, CinephileThreadEntity>>(emptyMap())
        private val messagesFlow = MutableStateFlow<List<CinephileMessageEntity>>(emptyList())

        override fun getThreads(): Flow<List<CinephileThreadEntity>> {
            return threadsFlow.map { it.values.sortedByDescending { t -> t.updatedAt } }
        }

        override suspend fun getThreadById(threadId: String): CinephileThreadEntity? {
            return threadsFlow.value[threadId]
        }

        override suspend fun insertThread(thread: CinephileThreadEntity) {
            threadsFlow.value = threadsFlow.value + (thread.id to thread)
        }

        override suspend fun updateThread(thread: CinephileThreadEntity) {
            threadsFlow.value = threadsFlow.value + (thread.id to thread)
        }

        override suspend fun deleteThread(threadId: String) {
            threadsFlow.value = threadsFlow.value - threadId
        }

        override fun getMessagesForThread(threadId: String): Flow<List<CinephileMessageEntity>> {
            return messagesFlow.map { list ->
                list.filter { it.threadId == threadId }.sortedBy { it.timestamp }
            }
        }

        override suspend fun insertMessage(message: CinephileMessageEntity) {
            messagesFlow.value = messagesFlow.value + message
        }

        override suspend fun deleteMessagesForThread(threadId: String) {
            messagesFlow.value = messagesFlow.value.filterNot { it.threadId == threadId }
        }

        override suspend fun clearAllThreads() {
            threadsFlow.value = emptyMap()
        }

        override suspend fun clearAllMessages() {
            messagesFlow.value = emptyList()
        }
    }
}
