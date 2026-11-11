package com.ssverma.common.ui.assistant

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ai.model.AiModelAvailability
import com.ssverma.shared.ai.domain.model.CinephileChatThread
import com.ssverma.shared.ai.domain.repository.CinephileChatRepository
import com.ssverma.shared.ai.service.CinephileAssistantService
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.testing.fakes.FakeAppConfigRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CinephileAssistantViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val assistantService: CinephileAssistantService = mockk(relaxed = true)
    private val libraryRepository: LibraryRepository = mockk(relaxed = true)
    private val chatRepository: CinephileChatRepository = mockk(relaxed = true)
    private val fakeAppConfigRepository = FakeAppConfigRepository()

    private val threadsFlow = MutableStateFlow<List<CinephileChatThread>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { chatRepository.getThreads() } returns threadsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when cinephile AI feature flag is disabled, model is not checked or available`() =
        runTest {
            fakeAppConfigRepository.setCinephileAiEnabled(false)

            val viewModel = CinephileAssistantViewModel(
                assistantService = assistantService,
                libraryRepository = libraryRepository,
                chatRepository = chatRepository,
                appConfigRepository = fakeAppConfigRepository,
            )

            advanceUntilIdle()

            assertThat(viewModel.uiState.value.isModelAvailable).isFalse()
            assertThat(viewModel.uiState.value.availabilityReason).contains("disabled by configuration")
            coVerify(exactly = 0) { assistantService.checkAvailability() }
        }

    @Test
    fun `when cinephile AI feature flag is enabled and engine available, model is marked available`() =
        runTest {
            fakeAppConfigRepository.setCinephileAiEnabled(true)
            coEvery { assistantService.checkAvailability() } returns AiModelAvailability.Available

            val viewModel = CinephileAssistantViewModel(
                assistantService = assistantService,
                libraryRepository = libraryRepository,
                chatRepository = chatRepository,
                appConfigRepository = fakeAppConfigRepository,
            )

            advanceUntilIdle()

            assertThat(viewModel.uiState.value.isModelAvailable).isTrue()
            assertThat(viewModel.uiState.value.availabilityReason).isNull()
            coVerify(atLeast = 1) { assistantService.checkAvailability() }
        }

    @Test
    fun `when engine unavailable, uiState reflects unavailable reason`() = runTest {
        fakeAppConfigRepository.setCinephileAiEnabled(true)
        coEvery { assistantService.checkAvailability() } returns AiModelAvailability.Unavailable("Hardware unsupported")

        val viewModel = CinephileAssistantViewModel(
            assistantService = assistantService,
            libraryRepository = libraryRepository,
            chatRepository = chatRepository,
            appConfigRepository = fakeAppConfigRepository,
        )

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isModelAvailable).isFalse()
        assertThat(viewModel.uiState.value.availabilityReason).isEqualTo("Hardware unsupported")
    }

    @Test
    fun `startNewThread clears current conversation and primes new session`() = runTest {
        fakeAppConfigRepository.setCinephileAiEnabled(true)
        coEvery { assistantService.checkAvailability() } returns AiModelAvailability.Available

        val viewModel = CinephileAssistantViewModel(
            assistantService = assistantService,
            libraryRepository = libraryRepository,
            chatRepository = chatRepository,
            appConfigRepository = fakeAppConfigRepository,
        )

        advanceUntilIdle()

        viewModel.updatePromptInput("Hello")
        viewModel.startNewThread()

        assertThat(viewModel.uiState.value.currentThreadId).isNull()
        assertThat(viewModel.uiState.value.promptInput).isEmpty()
        assertThat(viewModel.uiState.value.messages).isEmpty()
        coVerify(atLeast = 1) { assistantService.getOrCreateSession(any()) }
    }

    @Test
    fun `deleteThread deletes thread from repository`() = runTest {
        fakeAppConfigRepository.setCinephileAiEnabled(true)
        val viewModel = CinephileAssistantViewModel(
            assistantService = assistantService,
            libraryRepository = libraryRepository,
            chatRepository = chatRepository,
            appConfigRepository = fakeAppConfigRepository,
        )

        advanceUntilIdle()

        viewModel.deleteThread("thread-42")
        advanceUntilIdle()

        coVerify(exactly = 1) { chatRepository.deleteThread("thread-42") }
    }
}
