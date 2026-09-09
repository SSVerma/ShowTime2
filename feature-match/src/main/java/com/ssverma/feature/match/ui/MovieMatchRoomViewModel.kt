package com.ssverma.feature.match.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.billing.BillingRepository
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.ads.quota.RewardPassType
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.match.MatchMode
import com.ssverma.shared.domain.model.match.MatchRoomConfig
import com.ssverma.shared.domain.model.match.MovieMatchCard
import com.ssverma.shared.domain.model.match.SwipeDirection
import com.ssverma.shared.domain.repository.MatchRoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieMatchRoomViewModel @Inject constructor(
    private val matchRoomRepository: MatchRoomRepository,
    private val rewardManager: RewardManager,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieMatchRoomUiState())
    val uiState: StateFlow<MovieMatchRoomUiState> = _uiState.asStateFlow()

    private var currentRemoteRoomId: String? = null

    init {
        viewModelScope.launch {
            combine(
                billingRepository.isProActive,
                rewardManager.passStatus
            ) { isPro, passStatus ->
                isPro || passStatus.isMatchRoomUnlocked
            }.collect { isUnlocked ->
                _uiState.update { it.copy(isProOrPassActive = isUnlocked) }
            }
        }
    }

    fun initFromNavKey(roomCode: String?, initialMode: String?) {
        if (!roomCode.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    phase = MatchScreenPhase.JOIN,
                    mode = MatchMode.REMOTE,
                    joinCodeInput = roomCode
                )
            }
        } else if (initialMode.equals("remote", ignoreCase = true)) {
            _uiState.update {
                it.copy(mode = MatchMode.REMOTE)
            }
        }
    }

    fun setJoinCodeInput(code: String) {
        _uiState.update { it.copy(joinCodeInput = code.uppercase()) }
    }

    fun openSetupSheet() {
        _uiState.update { it.copy(showSetupSheet = true) }
    }

    fun closeSetupSheet() {
        _uiState.update { it.copy(showSetupSheet = false) }
    }

    fun openSynopsis(card: MovieMatchCard) {
        _uiState.update { it.copy(showSynopsisSheet = card) }
    }

    fun closeSynopsis() {
        _uiState.update { it.copy(showSynopsisSheet = null) }
    }

    fun startGame(config: MatchRoomConfig, player1: String, player2: String) {
        viewModelScope.launch {
            val isProOrPass = _uiState.value.isProOrPassActive
            val canStart = matchRoomRepository.canStartMatchSession(isProOrPass)

            if (!canStart) {
                _uiState.update { it.copy(showQuotaModal = true, showSetupSheet = false) }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = true,
                    showSetupSheet = false,
                    config = config,
                    mode = config.mode,
                    player1Name = player1,
                    player2Name = player2,
                    errorMessage = null
                )
            }

            matchRoomRepository.recordMatchSessionStarted()

            when (val deckResult = matchRoomRepository.fetchMatchDeck(config)) {
                is Result.Success -> {
                    val cards = deckResult.data
                    if (config.mode == MatchMode.COUCH) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                phase = MatchScreenPhase.SWIPING,
                                cards = cards,
                                topCardIndex = 0,
                                activePlayerIndex = 0,
                                player1Likes = emptySet(),
                                player2Likes = emptySet(),
                                matches = emptyList(),
                                swipeHistory = emptyList()
                            )
                        }
                    } else {
                        // Remote Room Mode
                        createRemoteRoom(cards, hostName = player1)
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unable to load movie deck. Please check your connection."
                        )
                    }
                }
            }
        }
    }

    private fun createRemoteRoom(cards: List<MovieMatchCard>, hostName: String) {
        viewModelScope.launch {
            when (val roomResult = matchRoomRepository.createRemoteRoom(hostName, cards)) {
                is Result.Success -> {
                    val room = roomResult.data
                    currentRemoteRoomId = room.id
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phase = MatchScreenPhase.SWIPING,
                            cards = cards,
                            roomCode = room.roomCode,
                            isHost = true,
                            topCardIndex = 0,
                            matches = emptyList()
                        )
                    }
                    observeRemoteRoom(room.id)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to create remote room."
                        )
                    }
                }
            }
        }
    }

    fun joinRemoteRoom(guestName: String) {
        val code = _uiState.value.joinCodeInput.trim()
        if (code.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val joinResult = matchRoomRepository.joinRemoteRoom(code, guestName)) {
                is Result.Success -> {
                    val room = joinResult.data
                    currentRemoteRoomId = room.id
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phase = MatchScreenPhase.SWIPING,
                            mode = MatchMode.REMOTE,
                            cards = room.deckCards,
                            roomCode = room.roomCode,
                            player1Name = room.hostName,
                            player2Name = guestName,
                            isHost = false,
                            topCardIndex = 0,
                            matches = room.matchedCards()
                        )
                    }
                    observeRemoteRoom(room.id)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Room not found. Check the code and try again."
                        )
                    }
                }
            }
        }
    }

    private fun observeRemoteRoom(roomId: String) {
        viewModelScope.launch {
            matchRoomRepository.observeRemoteRoom(roomId).collect { room ->
                if (room != null) {
                    val latestMatches = room.matchedCards()
                    val existingMatchIds = _uiState.value.matches.map { it.id }.toSet()
                    val newlyMatched =
                        latestMatches.firstOrNull { !existingMatchIds.contains(it.id) }

                    _uiState.update { current ->
                        current.copy(
                            matches = latestMatches,
                            celebratingMatch = newlyMatched ?: current.celebratingMatch
                        )
                    }
                }
            }
        }
    }

    fun onSwipe(card: MovieMatchCard, direction: SwipeDirection) {
        val state = _uiState.value
        val isLiked = direction == SwipeDirection.LIKE
        val newHistory = state.swipeHistory + (card.id to isLiked)

        if (state.mode == MatchMode.COUCH) {
            val isPlayer1 = state.activePlayerIndex == 0
            val p1Likes =
                if (isPlayer1 && isLiked) state.player1Likes + card.id else state.player1Likes
            val p2Likes =
                if (!isPlayer1 && isLiked) state.player2Likes + card.id else state.player2Likes

            var newMatch: MovieMatchCard? = null
            var updatedMatches = state.matches

            if (!isPlayer1 && isLiked && p1Likes.contains(card.id)) {
                newMatch = card
                updatedMatches = updatedMatches + card
            }

            val nextIndex = state.topCardIndex + 1
            if (nextIndex >= state.cards.size) {
                if (isPlayer1) {
                    // Hand off to Player 2
                    _uiState.update {
                        it.copy(
                            phase = MatchScreenPhase.HANDOFF,
                            player1Likes = p1Likes,
                            player2Likes = p2Likes,
                            swipeHistory = emptyList(),
                            celebratingMatch = newMatch
                        )
                    }
                } else {
                    // Round Finished
                    _uiState.update {
                        it.copy(
                            phase = MatchScreenPhase.SUMMARY,
                            player1Likes = p1Likes,
                            player2Likes = p2Likes,
                            matches = updatedMatches,
                            celebratingMatch = newMatch
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        topCardIndex = nextIndex,
                        player1Likes = p1Likes,
                        player2Likes = p2Likes,
                        matches = updatedMatches,
                        celebratingMatch = newMatch ?: it.celebratingMatch,
                        swipeHistory = newHistory
                    )
                }
            }
        } else {
            // Remote Room Mode
            currentRemoteRoomId?.let { roomId ->
                viewModelScope.launch {
                    matchRoomRepository.submitRemoteSwipe(
                        roomId = roomId,
                        isHost = state.isHost,
                        movieId = card.id,
                        direction = direction
                    )
                }
            }

            val nextIndex = state.topCardIndex + 1
            if (nextIndex >= state.cards.size) {
                _uiState.update {
                    it.copy(
                        phase = MatchScreenPhase.SUMMARY,
                        swipeHistory = newHistory
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        topCardIndex = nextIndex,
                        swipeHistory = newHistory
                    )
                }
            }
        }
    }

    fun onRewind() {
        val state = _uiState.value
        if (!state.canUndo) return

        val lastSwipe = state.swipeHistory.lastOrNull() ?: return
        val (swipedMovieId, wasLiked) = lastSwipe
        val prevIndex = (state.topCardIndex - 1).coerceAtLeast(0)

        val updatedP1Likes = if (state.activePlayerIndex == 0 && wasLiked) {
            state.player1Likes - swipedMovieId
        } else state.player1Likes

        val updatedP2Likes = if (state.activePlayerIndex == 1 && wasLiked) {
            state.player2Likes - swipedMovieId
        } else state.player2Likes

        _uiState.update {
            it.copy(
                topCardIndex = prevIndex,
                player1Likes = updatedP1Likes,
                player2Likes = updatedP2Likes,
                matches = it.matches.filter { m -> m.id != swipedMovieId },
                swipeHistory = it.swipeHistory.dropLast(1)
            )
        }
    }

    fun startPlayer2Swiping() {
        _uiState.update {
            it.copy(
                phase = MatchScreenPhase.SWIPING,
                activePlayerIndex = 1,
                topCardIndex = 0,
                swipeHistory = emptyList()
            )
        }
    }

    fun dismissCelebration() {
        _uiState.update { it.copy(celebratingMatch = null) }
    }

    fun saveToWatchlist(card: MovieMatchCard) {
        viewModelScope.launch {
            matchRoomRepository.saveMatchToWatchlist(card)
        }
    }

    fun grantRewardedPass() {
        viewModelScope.launch {
            rewardManager.grantRewardPass(RewardPassType.MATCH_ROOM)
            _uiState.update { it.copy(showQuotaModal = false) }
        }
    }

    fun dismissQuotaModal() {
        _uiState.update { it.copy(showQuotaModal = false) }
    }

    fun resetToSetup() {
        _uiState.update {
            it.copy(
                phase = MatchScreenPhase.SETUP,
                topCardIndex = 0,
                matches = emptyList(),
                celebratingMatch = null,
                swipeHistory = emptyList()
            )
        }
    }
}
