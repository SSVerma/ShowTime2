package com.ssverma.common.ui.assistant

import androidx.compose.runtime.Immutable
import com.ssverma.shared.ai.a2ui.model.A2UiDocument
import com.ssverma.shared.ai.domain.model.CinephileChatThread

/**
 * UI State representing the state of the Cinephile AI Assistant bottom sheet.
 */
@Immutable
data class CinephileAssistantUiState(
    val promptInput: String = "",
    val isModelAvailable: Boolean = true,
    val availabilityReason: String? = null,
    val isStreaming: Boolean = false,
    val activeToolName: String? = null,
    val messages: List<CinephileMessageItem> = emptyList(),
    val errorMessage: String? = null,
    val currentThreadId: String? = null,
    val currentThreadTitle: String? = null,
    val threads: List<CinephileChatThread> = emptyList(),
    val isThreadHistoryOpen: Boolean = false,
)

/**
 * Sealed items rendered inside the conversation thread.
 */
@Immutable
sealed interface CinephileMessageItem {

    @Immutable
    data class UserPrompt(
        val id: String,
        val text: String,
    ) : CinephileMessageItem

    @Immutable
    data class AgentDocument(
        val id: String,
        val document: A2UiDocument,
    ) : CinephileMessageItem

    @Immutable
    data class StreamingChunk(
        val id: String,
        val partialText: String,
        val activeToolName: String? = null,
    ) : CinephileMessageItem
}
