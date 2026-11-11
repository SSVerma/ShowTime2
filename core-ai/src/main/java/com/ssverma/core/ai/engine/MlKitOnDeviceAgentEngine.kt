package com.ssverma.core.ai.engine

import android.content.Context
import com.google.mlkit.genai.agent.Agent
import com.google.mlkit.genai.agent.AgentEvent
import com.google.mlkit.genai.agent.AgentRunner
import com.google.mlkit.genai.agent.AgentSession
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Content
import com.ssverma.core.ai.model.AiAgentEvent
import com.ssverma.core.ai.model.AiModelAvailability
import com.ssverma.core.ai.session.OnDeviceAgentSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitOnDeviceAgentEngine @Inject constructor(
    @param:ApplicationContext private val context: Context
) : OnDeviceAgentEngine {

    override suspend fun checkAvailability(): AiModelAvailability {
        return try {
            val agent = Agent.builder()
                .setName("ProbeAgent")
                .setInstructions("Test")
                .build()
            val runner = AgentRunner(context, agent)
            AiModelAvailability.Available
        } catch (exception: GenAiException) {
            when (exception.errorCode) {
                GenAiException.ErrorCode.NOT_AVAILABLE -> {
                    AiModelAvailability.Unavailable(
                        reason = exception.localizedMessage
                            ?: "On-device AI model is not available on this device."
                    )
                }

                GenAiException.ErrorCode.BUSY -> {
                    AiModelAvailability.Unavailable(
                        reason = "AICore is currently busy or thermal throttling is active."
                    )
                }

                else -> {
                    AiModelAvailability.Unavailable(
                        reason = exception.localizedMessage
                            ?: "AI runtime error (${exception.errorCode})"
                    )
                }
            }
        } catch (throwable: Throwable) {
            AiModelAvailability.Unavailable(
                reason = throwable.localizedMessage ?: "Unknown initialization failure."
            )
        }
    }

    override fun createSession(
        sessionId: String,
        agentName: String,
        systemInstructions: String,
        enabledAppFunctionIds: List<String>
    ): OnDeviceAgentSession {
        val agentBuilder = Agent.builder()
            .setName(agentName)
            .setInstructions(systemInstructions)
            .enableAppFunctions(true)

        if (enabledAppFunctionIds.isNotEmpty()) {
            agentBuilder.addAppFunctionIds(enabledAppFunctionIds)
        }

        val agent = agentBuilder.build()
        val runner = AgentRunner(context, agent)
        val rawSession = runner.getOrCreateSession(sessionId)

        return MlKitAgentSession(
            sessionId = sessionId,
            rawSession = rawSession
        )
    }

    override fun runSingleTurn(
        agentName: String,
        systemInstructions: String,
        prompt: String,
        enabledAppFunctionIds: List<String>
    ): Flow<AiAgentEvent> = flow {
        val agentBuilder = Agent.builder()
            .setName(agentName)
            .setInstructions(systemInstructions)
            .enableAppFunctions(true)

        if (enabledAppFunctionIds.isNotEmpty()) {
            agentBuilder.addAppFunctionIds(enabledAppFunctionIds)
        }

        val agent = agentBuilder.build()
        val runner = AgentRunner(context, agent)
        val content = Content.builder().text(prompt).build()

        val fullTextBuilder = StringBuilder()

        try {
            runner.runSingleTurnAsync(content).collect { event ->
                when (event) {
                    is AgentEvent.TextMessage -> {
                        if (event.isPartial) {
                            fullTextBuilder.append(event.text)
                            emit(AiAgentEvent.Token(event.text))
                        } else if (fullTextBuilder.isEmpty()) {
                            fullTextBuilder.append(event.text)
                            emit(AiAgentEvent.Token(event.text))
                        }
                    }

                    is AgentEvent.FunctionCall -> {
                        emit(AiAgentEvent.ToolStarted(event.name))
                    }

                    is AgentEvent.FunctionResponse -> {
                        emit(AiAgentEvent.ToolCompleted(event.name))
                    }

                    else -> {
                        // Forward-compatible handling for future ML Kit Agent events
                    }
                }
            }
            emit(AiAgentEvent.Completed(fullText = fullTextBuilder.toString()))
        } catch (exception: GenAiException) {
            val isRecoverable = exception.errorCode == GenAiException.ErrorCode.BUSY
            emit(
                AiAgentEvent.Error(
                    message = exception.localizedMessage ?: "GenAI error (${exception.errorCode})",
                    isRecoverable = isRecoverable
                )
            )
        } catch (throwable: Throwable) {
            emit(
                AiAgentEvent.Error(
                    message = throwable.localizedMessage ?: "Execution failure",
                    isRecoverable = false
                )
            )
        }
    }

    private class MlKitAgentSession(
        override val sessionId: String,
        private val rawSession: AgentSession
    ) : OnDeviceAgentSession {

        override fun sendPrompt(prompt: String): Flow<AiAgentEvent> = flow {
            val content = Content.builder().text(prompt).build()
            val fullTextBuilder = StringBuilder()

            try {
                rawSession.runAsync(content).collect { event ->
                    when (event) {
                        is AgentEvent.TextMessage -> {
                            if (event.isPartial) {
                                fullTextBuilder.append(event.text)
                                emit(AiAgentEvent.Token(event.text))
                            } else if (fullTextBuilder.isEmpty()) {
                                fullTextBuilder.append(event.text)
                                emit(AiAgentEvent.Token(event.text))
                            }
                        }

                        is AgentEvent.FunctionCall -> {
                            emit(AiAgentEvent.ToolStarted(event.name))
                        }

                        is AgentEvent.FunctionResponse -> {
                            emit(AiAgentEvent.ToolCompleted(event.name))
                        }

                        else -> {
                            // Forward-compatible handling
                        }
                    }
                }
                emit(AiAgentEvent.Completed(fullText = fullTextBuilder.toString()))
            } catch (exception: GenAiException) {
                val isRecoverable = exception.errorCode == GenAiException.ErrorCode.BUSY
                emit(
                    AiAgentEvent.Error(
                        message = exception.localizedMessage
                            ?: "GenAI error (${exception.errorCode})",
                        isRecoverable = isRecoverable
                    )
                )
            } catch (throwable: Throwable) {
                emit(
                    AiAgentEvent.Error(
                        message = throwable.localizedMessage ?: "Session turn failure",
                        isRecoverable = false
                    )
                )
            }
        }

        override fun close() {
            rawSession.close()
        }
    }
}
