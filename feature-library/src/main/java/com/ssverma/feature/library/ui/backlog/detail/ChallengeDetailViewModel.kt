package com.ssverma.feature.library.ui.backlog.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeProgress
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.usecase.challenge.GetBacklogChallengesUseCase
import com.ssverma.shared.domain.usecase.challenge.ManageChallengeUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeDetailViewModel @Inject constructor(
    private val getBacklogChallengesUseCase: GetBacklogChallengesUseCase,
    private val manageChallengeUseCase: ManageChallengeUseCase,
    private val saveDiaryEntryUseCase: SaveDiaryEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeDetailUiState())
    val uiState: StateFlow<ChallengeDetailUiState> = _uiState.asStateFlow()

    private var currentChallengeId: String? = null
    private var observeJob: Job? = null

    fun initChallenge(challengeId: String) {
        if (currentChallengeId == challengeId && observeJob != null) return
        currentChallengeId = challengeId

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getBacklogChallengesUseCase.getChallengeDetailFlow(challengeId)
                .collect { (progress, isJoined) ->
                    _uiState.update { current ->
                        current.copy(
                            progress = progress,
                            isJoined = isJoined,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun selectFilter(index: Int) {
        _uiState.update { it.copy(selectedFilterIndex = index) }
    }

    fun requestJoinConfirmation() {
        _uiState.update { it.copy(showJoinConfirmation = true) }
    }

    fun dismissJoinConfirmation() {
        _uiState.update { it.copy(showJoinConfirmation = false) }
    }

    fun confirmJoin() {
        val challenge = _uiState.value.progress?.challenge ?: return
        viewModelScope.launch {
            manageChallengeUseCase.joinChallenge(challenge)
            _uiState.update { it.copy(showJoinConfirmation = false) }
        }
    }

    fun requestLeaveConfirmation() {
        _uiState.update { it.copy(showLeaveConfirmation = true) }
    }

    fun dismissLeaveConfirmation() {
        _uiState.update { it.copy(showLeaveConfirmation = false) }
    }

    fun confirmLeave(onLeft: () -> Unit) {
        val challengeId = currentChallengeId ?: return
        viewModelScope.launch {
            manageChallengeUseCase.leaveChallenge(challengeId)
            _uiState.update { it.copy(showLeaveConfirmation = false) }
            onLeft()
        }
    }

    fun openLogDialog(item: ChallengeMediaItem) {
        _uiState.update { it.copy(mediaItemToLog = item) }
    }

    fun dismissLogDialog() {
        _uiState.update { it.copy(mediaItemToLog = null) }
    }

    fun saveDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            saveDiaryEntryUseCase(entry)
            _uiState.update { it.copy(mediaItemToLog = null) }
        }
    }

    fun generateShareableChallengeText(progress: ChallengeProgress): String {
        return buildString {
            append("🏆 Cinema Challenge: ${progress.challenge.title}\n")
            append("📊 Progress: ${progress.watchedCount}/${progress.totalCount} titles (${progress.progressPercentage}%)\n")
            append("🎖 Milestone: ${progress.milestoneTitle}\n")
            if (progress.isCompleted) {
                append("🎉 Challenge Completed on ShowTime!\n")
            } else if (progress.remainingItems.isNotEmpty()) {
                val nextUp = progress.remainingItems.firstOrNull()?.title
                if (!nextUp.isNullOrBlank()) {
                    append("🍿 Next Up: $nextUp\n")
                }
            }
            append("\nTracked on ShowTime App")
        }
    }
}
