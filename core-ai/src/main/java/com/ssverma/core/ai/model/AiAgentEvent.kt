package com.ssverma.core.ai.model

/**
 * High-level events emitted during on-device AI agent reasoning loops.
 */
sealed interface AiAgentEvent {
    /**
     * Incremental text token streamed from the AI model.
     *
     * @property token The incremental piece of generated text.
     */
    data class Token(val token: String) : AiAgentEvent

    /**
     * Dispatched when the agent decides to invoke an on-device tool (AppFunction).
     *
     * @property toolName Identifier of the tool being executed.
     */
    data class ToolStarted(val toolName: String) : AiAgentEvent

    /**
     * Dispatched when an on-device tool invocation has completed successfully.
     *
     * @property toolName Identifier of the tool that finished execution.
     */
    data class ToolCompleted(val toolName: String) : AiAgentEvent

    /**
     * Emitted when a multi-step turn or ephemeral task finishes completely.
     *
     * @property fullText The complete assembled textual response.
     */
    data class Completed(val fullText: String) : AiAgentEvent

    /**
     * Emitted when an error occurs during execution.
     *
     * @property message Descriptive failure message.
     * @property isRecoverable Whether the user can retry or session can continue.
     */
    data class Error(val message: String, val isRecoverable: Boolean) : AiAgentEvent
}
