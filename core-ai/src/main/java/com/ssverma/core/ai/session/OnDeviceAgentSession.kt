package com.ssverma.core.ai.session

import com.ssverma.core.ai.model.AiAgentEvent
import kotlinx.coroutines.flow.Flow

/**
 * An active, stateful multi-turn conversational session with an on-device AI agent.
 */
interface OnDeviceAgentSession : AutoCloseable {
    /**
     * Unique identifier for this session.
     */
    val sessionId: String

    /**
     * Sends a user prompt into the multi-turn session and streams incremental events.
     *
     * @param prompt The user's input prompt.
     * @return Cold flow of [AiAgentEvent]s emitted during the reasoning loop.
     */
    fun sendPrompt(prompt: String): Flow<AiAgentEvent>

    /**
     * Evicts session memory and releases AICore resources.
     */
    override fun close()
}
