package com.ssverma.common.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ai.model.AiModelAvailability
import com.ssverma.shared.ai.a2ui.model.A2UiAction
import com.ssverma.shared.ai.a2ui.model.A2UiComponent
import com.ssverma.shared.ai.a2ui.model.A2UiDocument
import com.ssverma.shared.ai.domain.model.CinephileChatMessage
import com.ssverma.shared.ai.domain.repository.CinephileChatRepository
import com.ssverma.shared.ai.service.CinephileAssistantService
import com.ssverma.shared.ai.service.CinephileResponseState
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CinephileAssistantViewModel @Inject constructor(
    private val assistantService: CinephileAssistantService,
    private val libraryRepository: LibraryRepository,
    private val chatRepository: CinephileChatRepository,
    private val appConfigRepository: AppConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CinephileAssistantUiState())
    val uiState: StateFlow<CinephileAssistantUiState> = _uiState.asStateFlow()

    private var messagesJob: Job? = null

    init {
        observeFeatureFlagAndAvailability()
        observeThreads()
    }

    private fun observeFeatureFlagAndAvailability() {
        viewModelScope.launch {
            appConfigRepository.isCinephileAiEnabled.collectLatest { isEnabled ->
                if (!isEnabled) {
                    _uiState.update { state ->
                        state.copy(
                            isModelAvailable = false,
                            availabilityReason = "Cinephile AI is disabled by configuration",
                        )
                    }
                } else {
                    checkAiAvailability()
                }
            }
        }
    }

    private fun observeThreads() {
        viewModelScope.launch {
            chatRepository.getThreads().collect { threadList ->
                _uiState.update { state ->
                    state.copy(threads = threadList)
                }
            }
        }
    }

    fun checkAiAvailability() {
        viewModelScope.launch {
            when (val availability = assistantService.checkAvailability()) {
                is AiModelAvailability.Available -> {
                    _uiState.update { state ->
                        state.copy(
                            isModelAvailable = true,
                            availabilityReason = null,
                        )
                    }
                }

                is AiModelAvailability.Downloading -> {
                    _uiState.update { state ->
                        state.copy(
                            isModelAvailable = false,
                            availabilityReason = "Downloading on-device AI model (${(availability.progressFraction * 100).toInt()}%)",
                        )
                    }
                }

                is AiModelAvailability.Unavailable -> {
                    _uiState.update { state ->
                        state.copy(
                            isModelAvailable = false,
                            availabilityReason = availability.reason,
                        )
                    }
                }
            }
        }
    }

    fun toggleThreadHistory(open: Boolean) {
        _uiState.update { state ->
            state.copy(isThreadHistoryOpen = open)
        }
    }

    fun startNewThread() {
        messagesJob?.cancel()
        messagesJob = null

        // Prime a new session in assistant service
        val freshSessionId = UUID.randomUUID().toString()
        assistantService.getOrCreateSession(sessionId = freshSessionId)

        _uiState.update { state ->
            state.copy(
                currentThreadId = null,
                currentThreadTitle = null,
                messages = emptyList(),
                promptInput = "",
                errorMessage = null,
                isStreaming = false,
                activeToolName = null,
                isThreadHistoryOpen = false,
            )
        }
    }

    fun selectThread(threadId: String) {
        val targetThread = _uiState.value.threads.firstOrNull { it.id == threadId }
        messagesJob?.cancel()

        _uiState.update { state ->
            state.copy(
                currentThreadId = threadId,
                currentThreadTitle = targetThread?.title,
                isThreadHistoryOpen = false,
                isStreaming = false,
                activeToolName = null,
                errorMessage = null,
            )
        }

        // Connect session to the thread ID
        assistantService.getOrCreateSession(sessionId = threadId)

        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(threadId).collect { chatMessages ->
                val uiItems = chatMessages.map { msg ->
                    when (msg) {
                        is CinephileChatMessage.User -> {
                            CinephileMessageItem.UserPrompt(
                                id = msg.id,
                                text = msg.text,
                            )
                        }

                        is CinephileChatMessage.Assistant -> {
                            val doc = msg.document ?: A2UiDocument(
                                id = msg.id,
                                components = listOf(
                                    A2UiComponent.Text(
                                        id = msg.id,
                                        content = msg.text,
                                    )
                                )
                            )
                            CinephileMessageItem.AgentDocument(
                                id = msg.id,
                                document = doc,
                            )
                        }
                    }
                }
                _uiState.update { state ->
                    // Preserve any currently streaming chunk if applicable
                    val streamingChunk =
                        state.messages.filterIsInstance<CinephileMessageItem.StreamingChunk>()
                    state.copy(messages = uiItems + streamingChunk)
                }
            }
        }
    }

    fun deleteThread(threadId: String) {
        viewModelScope.launch {
            chatRepository.deleteThread(threadId)
            if (_uiState.value.currentThreadId == threadId) {
                startNewThread()
            }
        }
    }

    fun updatePromptInput(input: String) {
        _uiState.update { state ->
            state.copy(promptInput = input)
        }
    }

    fun submitPrompt(prompt: String = _uiState.value.promptInput) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || _uiState.value.isStreaming || !_uiState.value.isModelAvailable) return

        val activeThreadId = _uiState.value.currentThreadId ?: UUID.randomUUID().toString()
        val isBrandNewThread = _uiState.value.currentThreadId == null

        val userMessageId = UUID.randomUUID().toString()
        val streamingId = UUID.randomUUID().toString()

        val userMessage = CinephileMessageItem.UserPrompt(
            id = userMessageId,
            text = trimmed,
        )

        val initialStreamingChunk = CinephileMessageItem.StreamingChunk(
            id = streamingId,
            partialText = "",
            activeToolName = null,
        )

        val threadTitle = trimmed.take(40).trim().ifEmpty { "Chat" }

        _uiState.update { state ->
            state.copy(
                currentThreadId = activeThreadId,
                currentThreadTitle = state.currentThreadTitle ?: threadTitle,
                promptInput = "",
                isStreaming = true,
                errorMessage = null,
                messages = state.messages + userMessage + initialStreamingChunk,
            )
        }

        viewModelScope.launch {
            // Persist user message immediately
            chatRepository.saveUserMessage(
                threadId = activeThreadId,
                text = trimmed,
                messageId = userMessageId,
            )

            if (isBrandNewThread && messagesJob == null) {
                // Start observing messages for this newly created thread
                selectThread(activeThreadId)
            }

            val priorPrompts = _uiState.value.messages
                .filterIsInstance<CinephileMessageItem.UserPrompt>()
                .map { it.text }
                .dropLast(1)

            assistantService.sendMessage(
                prompt = trimmed,
                sessionId = activeThreadId,
                priorUserPrompts = priorPrompts,
            ).collect { responseState ->
                when (responseState) {
                    is CinephileResponseState.Streaming -> {
                        _uiState.update { state ->
                            val updatedMessages =
                                state.messages.filterNot { it is CinephileMessageItem.StreamingChunk } +
                                        CinephileMessageItem.StreamingChunk(
                                            id = streamingId,
                                            partialText = responseState.partialText,
                                            activeToolName = responseState.activeTool,
                                        )
                            state.copy(
                                isStreaming = true,
                                activeToolName = responseState.activeTool,
                                messages = updatedMessages,
                            )
                        }
                    }

                    is CinephileResponseState.Success -> {
                        // Persist assistant message in Room
                        chatRepository.saveAssistantMessage(
                            threadId = activeThreadId,
                            text = "",
                            document = responseState.document,
                            messageId = streamingId,
                        )

                        _uiState.update { state ->
                            val updatedMessages =
                                state.messages.filterNot { it is CinephileMessageItem.StreamingChunk } +
                                        CinephileMessageItem.AgentDocument(
                                            id = streamingId,
                                            document = responseState.document,
                                        )
                            state.copy(
                                isStreaming = false,
                                activeToolName = null,
                                messages = updatedMessages,
                            )
                        }
                    }

                    is CinephileResponseState.Failure -> {
                        _uiState.update { state ->
                            val updatedMessages =
                                state.messages.filterNot { it is CinephileMessageItem.StreamingChunk }
                            state.copy(
                                isStreaming = false,
                                activeToolName = null,
                                errorMessage = responseState.message,
                                messages = updatedMessages,
                            )
                        }
                    }
                }
            }
        }
    }

    fun handleA2UiAction(
        action: A2UiAction,
        onOpenMedia: (Int, String) -> Unit,
    ) {
        when (action) {
            is A2UiAction.OpenMedia -> {
                onOpenMedia(action.tmdbId, action.mediaType)
            }

            is A2UiAction.ToggleWatchlist -> {
                viewModelScope.launch {
                    libraryRepository.toggleWatchlist(
                        mediaId = action.tmdbId,
                        mediaType = MediaType.Movie,
                        title = action.title,
                        posterImageUrl = "",
                        backdropImageUrl = "",
                        voteAvg = 0.0f,
                        releaseDate = "",
                    )
                }
            }

            is A2UiAction.PromptQuery -> {
                submitPrompt(action.prompt)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
        assistantService.close()
    }
}
