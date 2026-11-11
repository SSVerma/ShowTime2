package com.ssverma.shared.ai.domain.model

import com.ssverma.shared.ai.a2ui.model.A2UiDocument

data class CinephileChatThread(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

sealed interface CinephileChatMessage {
    val id: String
    val threadId: String
    val timestamp: Long

    data class User(
        override val id: String,
        override val threadId: String,
        val text: String,
        override val timestamp: Long
    ) : CinephileChatMessage

    data class Assistant(
        override val id: String,
        override val threadId: String,
        val text: String,
        val document: A2UiDocument?,
        override val timestamp: Long
    ) : CinephileChatMessage
}
