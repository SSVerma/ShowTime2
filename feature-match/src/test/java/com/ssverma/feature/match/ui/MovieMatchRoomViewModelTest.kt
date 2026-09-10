package com.ssverma.feature.match.ui

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.billing.BillingRepository
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.ads.quota.RewardPassStatus
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.match.MatchDeckType
import com.ssverma.shared.domain.model.match.MatchMode
import com.ssverma.shared.domain.model.match.MatchRoom
import com.ssverma.shared.domain.model.match.MatchRoomConfig
import com.ssverma.shared.domain.model.match.MatchRoomStatus
import com.ssverma.shared.domain.model.match.MovieMatchCard
import com.ssverma.shared.domain.model.match.SwipeDirection
import com.ssverma.shared.domain.repository.MatchRoomRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieMatchRoomViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val matchRoomRepository: MatchRoomRepository = mockk(relaxed = true)
    private val rewardManager: RewardManager = mockk(relaxed = true)
    private val billingRepository: BillingRepository = mockk(relaxed = true)

    private val isProFlow = MutableStateFlow(false)
    private val passStatusFlow = MutableStateFlow(RewardPassStatus())

    private lateinit var viewModel: MovieMatchRoomViewModel

    private val fakeCard1 = MovieMatchCard(
        id = 1,
        title = "Inception",
        posterImageUrl = "https://example.com/1.jpg",
        backdropImageUrl = "https://example.com/1b.jpg",
        releaseYear = "2010",
        voteAvg = 8.8f,
        overview = "Dream within a dream",
        genreNames = listOf("Action", "Sci-Fi"),
        watchProviders = emptyList(),
        runtime = 148
    )

    private val fakeCard2 = MovieMatchCard(
        id = 2,
        title = "Interstellar",
        posterImageUrl = "https://example.com/2.jpg",
        backdropImageUrl = "https://example.com/2b.jpg",
        releaseYear = "2014",
        voteAvg = 8.6f,
        overview = "Wormhole journey",
        genreNames = listOf("Adventure", "Drama"),
        watchProviders = emptyList(),
        runtime = 169
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { billingRepository.isProActive } returns isProFlow
        every { rewardManager.passStatus } returns passStatusFlow
        coEvery { matchRoomRepository.canStartMatchSession(any()) } returns true
        coEvery { matchRoomRepository.fetchMatchDeck(any()) } returns Result.Success(
            listOf(fakeCard1, fakeCard2)
        )

        viewModel = MovieMatchRoomViewModel(
            matchRoomRepository = matchRoomRepository,
            rewardManager = rewardManager,
            billingRepository = billingRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initFromNavKey with room code sets phase to JOIN and populates code`() = runTest {
        viewModel.initFromNavKey(roomCode = "ST-9999", initialMode = null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.phase).isEqualTo(MatchScreenPhase.JOIN)
        assertThat(state.mode).isEqualTo(MatchMode.REMOTE)
        assertThat(state.joinCodeInput).isEqualTo("ST-9999")
    }

    @Test
    fun `openJoinRoom and backToSetup transitions phase between SETUP and JOIN`() = runTest {
        assertThat(viewModel.uiState.value.phase).isEqualTo(MatchScreenPhase.SETUP)

        viewModel.openJoinRoom()
        assertThat(viewModel.uiState.value.phase).isEqualTo(MatchScreenPhase.JOIN)

        viewModel.backToSetup()
        assertThat(viewModel.uiState.value.phase).isEqualTo(MatchScreenPhase.SETUP)
    }

    @Test
    fun `startGame starts swiping phase when quota permits`() = runTest {
        val config = MatchRoomConfig(mode = MatchMode.COUCH, deckType = MatchDeckType.TRENDING)
        viewModel.startGame(config, "Alice", "Bob")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.phase).isEqualTo(MatchScreenPhase.SWIPING)
        assertThat(state.activePlayerName).isEqualTo("Alice")
        assertThat(state.cards).hasSize(2)
        assertThat(state.topCardIndex).isEqualTo(0)
    }

    @Test
    fun `startGame shows quota modal when free quota exceeded`() = runTest {
        coEvery { matchRoomRepository.canStartMatchSession(isProActive = false) } returns false

        val config = MatchRoomConfig(mode = MatchMode.COUCH)
        viewModel.startGame(config, "Alice", "Bob")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.showQuotaModal).isTrue()
        assertThat(state.phase).isEqualTo(MatchScreenPhase.SETUP)
    }

    @Test
    fun `couch mode swipe handoff and mutual match detection flow`() = runTest {
        viewModel.startGame(MatchRoomConfig(mode = MatchMode.COUCH), "Alice", "Bob")
        advanceUntilIdle()

        // Alice likes card 1, passes card 2
        viewModel.onSwipe(fakeCard1, SwipeDirection.LIKE)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.topCardIndex).isEqualTo(1)

        viewModel.onSwipe(fakeCard2, SwipeDirection.PASS)
        advanceUntilIdle()

        // Handoff to Bob
        assertThat(viewModel.uiState.value.phase).isEqualTo(MatchScreenPhase.HANDOFF)

        viewModel.startPlayer2Swiping()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.phase).isEqualTo(MatchScreenPhase.SWIPING)
        assertThat(viewModel.uiState.value.activePlayerName).isEqualTo("Bob")
        assertThat(viewModel.uiState.value.topCardIndex).isEqualTo(0)

        // Bob likes card 1 -> Mutual Match!
        viewModel.onSwipe(fakeCard1, SwipeDirection.LIKE)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.celebratingMatch).isEqualTo(fakeCard1)
        assertThat(viewModel.uiState.value.matches).contains(fakeCard1)

        viewModel.dismissCelebration()
        assertThat(viewModel.uiState.value.celebratingMatch).isNull()

        // Bob passes card 2 -> Deck finished -> SUMMARY phase
        viewModel.onSwipe(fakeCard2, SwipeDirection.PASS)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.phase).isEqualTo(MatchScreenPhase.SUMMARY)
        assertThat(viewModel.uiState.value.matches).containsExactly(fakeCard1)
    }

    @Test
    fun `saveToWatchlist saves to repository and updates savedWatchlistIds`() = runTest {
        coEvery { matchRoomRepository.saveMatchToWatchlist(fakeCard1) } returns Result.Success(Unit)

        viewModel.saveToWatchlist(fakeCard1)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.savedWatchlistIds).contains(fakeCard1.id)
        coVerify { matchRoomRepository.saveMatchToWatchlist(fakeCard1) }
    }

    @Test
    fun `handleRewindClick triggers prompt when not pro and triggers rewind when pro`() = runTest {
        viewModel.startGame(MatchRoomConfig(mode = MatchMode.COUCH), "Alice", "Bob")
        advanceUntilIdle()

        // Swipe one card
        viewModel.onSwipe(fakeCard1, SwipeDirection.LIKE)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.topCardIndex).isEqualTo(1)

        var proPromptCalled = false
        // Non-pro attempt: should trigger pro prompt callback
        viewModel.handleRewindClick(onOpenPro = { proPromptCalled = true })
        advanceUntilIdle()
        assertThat(proPromptCalled).isTrue()
        assertThat(viewModel.uiState.value.topCardIndex).isEqualTo(1)

        // Pro user: rewinds the card
        isProFlow.value = true
        advanceUntilIdle()

        var proPromptCalledAgain = false
        viewModel.handleRewindClick(onOpenPro = { proPromptCalledAgain = true })
        advanceUntilIdle()

        assertThat(proPromptCalledAgain).isFalse()
        assertThat(viewModel.uiState.value.topCardIndex).isEqualTo(0)
    }

    @Test
    fun `partnerName resolves correct other player name in couch and remote mode`() = runTest {
        // In Couch mode
        val couchState = MovieMatchRoomUiState(
            config = MatchRoomConfig(mode = MatchMode.COUCH),
            player1Name = "Alice",
            player2Name = "Bob",
            activePlayerIndex = 0
        )
        assertThat(couchState.partnerName).isEqualTo("Bob")

        val couchStatePlayer2 = couchState.copy(activePlayerIndex = 1)
        assertThat(couchStatePlayer2.partnerName).isEqualTo("Alice")

        // In Remote mode
        val remoteStateHost = MovieMatchRoomUiState(
            config = MatchRoomConfig(mode = MatchMode.REMOTE),
            player1Name = "Host User",
            player2Name = "Guest Friend",
            isHost = true
        )
        assertThat(remoteStateHost.partnerName).isEqualTo("Guest Friend")

        val remoteStateGuest = remoteStateHost.copy(isHost = false)
        assertThat(remoteStateGuest.partnerName).isEqualTo("Host User")
    }

    @Test
    fun `remote room sync updates guest connection status and mutual matches`() = runTest {
        val fakeRoom = MatchRoom(
            id = "st-1234",
            roomCode = "ST-1234",
            hostUserId = "host-id",
            hostName = "Host User",
            guestUserId = "guest-id",
            guestName = "Guest Friend",
            mode = MatchMode.REMOTE,
            status = MatchRoomStatus.SWIPING,
            deckCards = listOf(fakeCard1, fakeCard2),
            hostLikes = listOf(1),
            hostPasses = emptyList(),
            guestLikes = listOf(1),
            guestPasses = emptyList(),
            matches = listOf(1),
            createdAtEpochMs = 1000L
        )

        coEvery { matchRoomRepository.createRemoteRoom(any(), any()) } returns Result.Success(
            fakeRoom
        )
        every { matchRoomRepository.observeRemoteRoom("st-1234") } returns flowOf(fakeRoom)

        viewModel.startGame(MatchRoomConfig(mode = MatchMode.REMOTE), "Host User", "Guest Friend")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isGuestConnected).isTrue()
        assertThat(state.matches).contains(fakeCard1)
    }
}
