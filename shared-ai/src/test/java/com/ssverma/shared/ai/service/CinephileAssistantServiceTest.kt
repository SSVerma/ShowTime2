package com.ssverma.shared.ai.service

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ai.engine.OnDeviceAgentEngine
import com.ssverma.core.ai.model.AiAgentEvent
import com.ssverma.core.ai.model.AiModelAvailability
import com.ssverma.core.ai.session.OnDeviceAgentSession
import com.ssverma.shared.ai.a2ui.model.A2UiComponent
import com.ssverma.shared.ai.a2ui.parser.A2UiParser
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.DiscoveryRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CinephileAssistantServiceTest {

    private lateinit var fakeEngine: FakeAgentEngine
    private lateinit var parser: A2UiParser
    private lateinit var service: CinephileAssistantService
    private val appConfigRepository: AppConfigRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        fakeEngine = FakeAgentEngine()
        parser = A2UiParser()
        service = CinephileAssistantService(
            agentEngine = fakeEngine,
            a2UiParser = parser,
            appConfigRepository = appConfigRepository,
        )
    }

    @Test
    fun `checkAvailability returns engine status`() = runTest {
        val result = service.checkAvailability()
        assertThat(result).isEqualTo(AiModelAvailability.Available)
    }

    @Test
    fun `sendMessage parses streamed events and delivers final A2Ui document`() = runTest {
        val flowEvents = listOf(
            AiAgentEvent.Token("Here is "),
            AiAgentEvent.Token("a recommendation"),
            AiAgentEvent.Completed("Here is a recommendation"),
        )
        fakeEngine.sessionEvents = flowEvents

        val states = service.sendMessage(prompt = "Recommend a movie").toList()

        assertThat(states).isNotEmpty()
        val successState = states.last() as CinephileResponseState.Success
        assertThat(successState.document.components).hasSize(1)
        val textComp = successState.document.components.first() as A2UiComponent.Text
        assertThat(textComp.content).isEqualTo("Here is a recommendation")
    }

    @Test
    fun `sendMessage with generic recommendation prompt injects userSeededGenres into discovery filter`() =
        runTest {
            every { appConfigRepository.userSeededGenres } returns flowOf(setOf(28)) // Action
            every { appConfigRepository.watchProviderRegion } returns MutableStateFlow("US")

            val fakeRepo = FakeDiscoveryRepository()
            val customService = CinephileAssistantService(
                agentEngine = fakeEngine,
                a2UiParser = parser,
                discoveryRepository = fakeRepo,
                appConfigRepository = appConfigRepository,
            )

            customService.sendMessage(prompt = "what should I watch tonight?").toList()

            assertThat(fakeRepo.lastFilter).isNotNull()
            assertThat(fakeRepo.lastFilter?.selectedGenreIds).contains(28)
        }

    @Test
    fun `sendMessage with explicit genre prompt strictly overrides userSeededGenres`() = runTest {
        every { appConfigRepository.userSeededGenres } returns flowOf(setOf(28)) // Action saved in onboarding
        every { appConfigRepository.watchProviderRegion } returns MutableStateFlow("US")

        val fakeRepo = FakeDiscoveryRepository()
        val customService = CinephileAssistantService(
            agentEngine = fakeEngine,
            a2UiParser = parser,
            discoveryRepository = fakeRepo,
            appConfigRepository = appConfigRepository,
        )

        // User asks for adventure movies
        customService.sendMessage(prompt = "recommend some adventure movies").toList()

        assertThat(fakeRepo.lastFilter).isNotNull()
        // Must contain Adventure (12)
        assertThat(fakeRepo.lastFilter?.selectedGenreIds).contains(12)
        // Must NOT contain saved Action (28)
        assertThat(fakeRepo.lastFilter?.selectedGenreIds).doesNotContain(28)
    }

    @Test
    fun `sendMessage with factual query bypasses catalog discovery grounding`() = runTest {
        val fakeRepo = FakeDiscoveryRepository()
        val customService = CinephileAssistantService(
            agentEngine = fakeEngine,
            a2UiParser = parser,
            discoveryRepository = fakeRepo,
            appConfigRepository = appConfigRepository,
        )

        customService.sendMessage(prompt = "who created interstellar").toList()

        assertThat(fakeRepo.lastFilter).isNull()
    }

    @Test
    fun `sendMessage with studio franchise prompt like all marvel movies does not filter by userSeededGenres`() =
        runTest {
            every { appConfigRepository.userSeededGenres } returns flowOf(setOf(18)) // Drama saved in onboarding
            every { appConfigRepository.watchProviderRegion } returns MutableStateFlow("US")

            val fakeRepo = FakeDiscoveryRepository()
            val customService = CinephileAssistantService(
                agentEngine = fakeEngine,
                a2UiParser = parser,
                discoveryRepository = fakeRepo,
                appConfigRepository = appConfigRepository,
            )

            customService.sendMessage(prompt = "all marvel movies").toList()

            assertThat(fakeRepo.lastFilter).isNotNull()
            assertThat(fakeRepo.lastFilter?.studioHub?.name).isEqualTo("MARVEL")
            // Must NOT contain saved Drama (18) because studio queries shouldn't be constrained by onboarding Drama
            assertThat(fakeRepo.lastFilter?.selectedGenreIds).isEmpty()
        }

    @Test
    fun `sendMessage with follow-up refinement retains prior discovery context`() = runTest {
        val fakeRepo = FakeDiscoveryRepository()
        val customService = CinephileAssistantService(
            agentEngine = fakeEngine,
            a2UiParser = parser,
            discoveryRepository = fakeRepo,
            appConfigRepository = appConfigRepository,
        )

        val priorPrompts = listOf("what is best movie in last yer", "how you decided ?")
        customService.sendMessage(
            prompt = "in India ?",
            priorUserPrompts = priorPrompts,
        ).toList()

        assertThat(fakeRepo.lastFilter).isNotNull()
        // Origin country should be IN and watchRegion should be IN from the follow-up
        assertThat(fakeRepo.lastFilter?.originCountry).isEqualTo("IN")
        assertThat(fakeRepo.lastFilter?.watchRegion).isEqualTo("IN")
        // Sort order should be preserved from "best" in the initial prompt
        assertThat(fakeRepo.lastFilter?.sortOrder?.apiValue).isEqualTo("vote_average.desc")
    }

    private class FakeDiscoveryRepository : DiscoveryRepository {
        var lastFilter: UniversalDiscoveryFilter? = null
        var lastSearchQuery: String? = null
        var returnItems: List<UniversalMediaItem> = listOf(
            UniversalMediaItem(
                id = 1,
                mediaType = com.ssverma.shared.domain.model.MediaType.Movie,
                title = "Test Movie",
                overview = "Overview",
                posterImageUrl = "",
                backdropImageUrl = "",
                voteAvg = 8.0f,
                voteCount = 100,
                releaseDate = "2024-01-01",
                displayYear = "2024",
            )
        )

        override suspend fun discoverUniversal(
            filter: UniversalDiscoveryFilter,
            page: Int,
        ): Result<List<UniversalMediaItem>, Failure.CoreFailure> {
            lastFilter = filter
            return Result.Success(returnItems)
        }

        override suspend fun searchUniversal(
            query: String,
            page: Int,
        ): Result<List<UniversalMediaItem>, Failure.CoreFailure> {
            lastSearchQuery = query
            return Result.Success(returnItems)
        }

        override suspend fun discoverMovies(discoverConfig: MovieDiscoverConfig): Result<List<Movie>, Failure.CoreFailure> =
            Result.Success(emptyList())

        override suspend fun discoverTvShows(discoverConfig: TvDiscoverConfig): Result<List<TvShow>, Failure.CoreFailure> =
            Result.Success(emptyList())

        override suspend fun fetchMovieGenre(): Result<List<Genre>, Failure.CoreFailure> =
            Result.Success(emptyList())

        override suspend fun fetchTvShowGenre(): Result<List<Genre>, Failure.CoreFailure> =
            Result.Success(emptyList())

        override suspend fun fetchWatchProviders(isMovie: Boolean): Result<List<ProviderInfo>, Failure.CoreFailure> =
            Result.Success(emptyList())
    }

    private class FakeAgentEngine : OnDeviceAgentEngine {
        var sessionEvents: List<AiAgentEvent> = emptyList()

        override suspend fun checkAvailability(): AiModelAvailability =
            AiModelAvailability.Available

        override fun createSession(
            sessionId: String,
            agentName: String,
            systemInstructions: String,
            enabledAppFunctionIds: List<String>,
        ): OnDeviceAgentSession {
            return FakeAgentSession(
                sessionId = sessionId,
                events = sessionEvents,
            )
        }

        override fun runSingleTurn(
            agentName: String,
            systemInstructions: String,
            prompt: String,
            enabledAppFunctionIds: List<String>,
        ): Flow<AiAgentEvent> = flowOf(*sessionEvents.toTypedArray())
    }

    private class FakeAgentSession(
        override val sessionId: String,
        private val events: List<AiAgentEvent>,
    ) : OnDeviceAgentSession {

        override fun sendPrompt(prompt: String): Flow<AiAgentEvent> {
            return flowOf(*events.toTypedArray())
        }

        override fun close() {
            // no-op
        }
    }
}
