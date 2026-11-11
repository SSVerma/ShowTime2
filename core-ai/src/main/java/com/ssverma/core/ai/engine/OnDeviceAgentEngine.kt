package com.ssverma.core.ai.engine

import com.ssverma.core.ai.model.AiAgentEvent
import com.ssverma.core.ai.model.AiModelAvailability
import com.ssverma.core.ai.session.OnDeviceAgentSession
import kotlinx.coroutines.flow.Flow

/**
 * Entry-point abstraction for configuring and running on-device AI agents powered by Gemini Nano.
 */
interface OnDeviceAgentEngine {
    /**
     * Checks whether the on-device AI model is available on this hardware.
     */
    suspend fun checkAvailability(): AiModelAvailability

    /**
     * Creates or retrieves an existing multi-turn conversation session.
     *
     * @param sessionId Stable session identifier.
     * @param agentName Name of the agent for logging and attribution.
     * @param systemInstructions Guiding instructions that define agent persona and behavioral boundaries.
     * @param enabledAppFunctionIds Optional list of AppFunction tool IDs to expose to this agent.
     */
    fun createSession(
        sessionId: String,
        agentName: String,
        systemInstructions: String,
        enabledAppFunctionIds: List<String> = emptyList()
    ): OnDeviceAgentSession

    /**
     * Executes a single-turn, stateless query ephemerally without session persistence.
     *
     * @param agentName Name of the agent.
     * @param systemInstructions System instructions for the query.
     * @param prompt User's query prompt.
     * @param enabledAppFunctionIds Optional list of AppFunction tool IDs.
     */
    fun runSingleTurn(
        agentName: String,
        systemInstructions: String,
        prompt: String,
        enabledAppFunctionIds: List<String> = emptyList()
    ): Flow<AiAgentEvent>
}
